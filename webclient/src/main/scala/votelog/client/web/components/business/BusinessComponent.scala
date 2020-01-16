package votelog.client.web.components.business

import mhtml.Rx
import votelog.client.web.components.html.StaticSelect
import votelog.client.web.components.html.tools.set
import votelog.client.web.components.{CrudIndexComponent, CrudShowComponent, Paging}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod}
import votelog.persistence.BusinessStore

import scala.concurrent.Future
import scala.xml.{Elem, Group, Node}

object BusinessComponent {
  case class Configuration(defaultContext: Context, defaultPageSize: PageSize, pageSizes: Seq[PageSize])
}

class BusinessComponent(
  // need to figure out better way to put all of this together
  component: votelog.domain.authorization.Component,
  configuration: BusinessComponent.Configuration,
  val store: BusinessStore[Future],
  language: Rx[Language]
) extends CrudIndexComponent[Business, Business.Id] { self =>

  def id(id: String): String = component.child(id).location

  val legislativePeriod =
    new StaticSelect(
      legend = "legislative period",
      options = LegislativePeriod.ids,
      default = LegislativePeriod.Default.id,
      clazz = "legislativePeriod",
      id = "legislativePeriod"
    )

  val pagingConfiguration = Paging.Configuration(self.configuration.defaultPageSize, configuration.pageSizes)
  val paging: Paging = new Paging(self.component.child("paging"), pagingConfiguration)

  val queryParameters: Rx[Context] =
    for {
      language <- language
      legislativePeriod <- legislativePeriod.model
    } yield Context(legislativePeriod, language)

  val indexQueryParameters: Rx[store.IndexQueryParameters] =
    for {
      offset <- paging.offset
      pageSize <- paging.pageSize
      context <- queryParameters
    } yield IndexQueryParameters(pageSize, offset, context)

  def renderBusinessPreview(id: Business.Id, business: Business): Rx[Elem] = Rx {
    <article class="ngo" data-selected={ self.selectedId.map(_.contains(id)) }>
      <dl>
        <dt>Name</dt>
        <dd>{business.name}</dd>
      </dl>
    </article>
  }

  def renderEntity(maybeBusiness: Option[Business]): Elem = {
    maybeBusiness match {
      case Some(business) =>
        <article class="business">
          <dl>
            <dd>NGO</dd>
            <dt>{business.name}</dt>
          </dl>
        </article>

      case None =>
        <article class="business loading" />
    }
  }

  object index {
    val model = self.ids
    val view =
      <section>
        <controls>
          <fieldset>
            { paging.view }
          </fieldset>

        </controls>
        { self.renderIndex(renderBusinessPreview) }
      </section>
  }

  object read {
    val model =  self.selectedId
    val view: Rx[Elem] = self.selectedEntity.map(renderEntity)
  }

  val view = Group {
    <section id={id("index")} >
      { index.view }
    </section>
    <section id={id("read")} >
      { read.view }
    </section>
  }
}
