package votelog.client.web.components.business

import mhtml.Rx
import votelog.client.web.components.html.StaticSelect
import votelog.client.web.components.{CrudIndexComponent, Paging}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.data.Sorting.Direction.Descending
import votelog.domain.politics
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod}
import votelog.persistence.BusinessStore

import scala.concurrent.Future
import scala.xml.{Elem, Group}

object BusinessComponent {
  case class Configuration(defaultContext: Context, defaultPageSize: PageSize, pageSizes: Seq[PageSize])
}

class BusinessComponent(
  component: votelog.domain.authorization.Component,
  configuration: BusinessComponent.Configuration,
  val store: BusinessStore[Future],
  language: Rx[Language]
) extends CrudIndexComponent[Business, Business.Id, Business.Partial, Language, IndexQueryParameters[Context, Business.Field, Business.Field]] { self =>

  def id(id: String): String = component.child(id).location

  val legislativePeriod =
    new StaticSelect(
      legend = "legislative period",
      options = LegislativePeriod.ids,
      default = LegislativePeriod.Default.id,
      clazz = "legislativePeriod",
      id = "legislativePeriod"
    )

  lazy val pagingConfiguration = Paging.Configuration(self.configuration.defaultPageSize, configuration.pageSizes)
  lazy val paging: Paging = new Paging(self.component.child("paging"), pagingConfiguration)
  lazy val queryParameters: Rx[Language] = language

  lazy val indexQueryParameters: Rx[IndexQueryParameters[Context, Business.Field, Business.Field]] =
    for {
      offset <- paging.offset
      pageSize <- paging.pageSize
      language <- language
      legislativePeriod <- legislativePeriod.model
    } yield IndexQueryParameters(
      pageSize,
      offset,
      Context(legislativePeriod, language),
      List(Business.Field.Title -> Descending),
      Set.empty)

  val errors: Rx[Iterable[Throwable]] = Rx(Nil)

  def renderEntityPreview(id: Business.Id, business: Business): Elem =
    <article class="business entity" data-selected={ self.selectedId.map(_.contains(id)) }>
      <dl>
        <dt>Title</dt>
        <dd>{business.title.getOrElse("no title")}</dd>
      </dl>
    </article>

  def renderEntity(maybeBusiness: Option[Business]): Elem = {
    maybeBusiness match {
      case Some(business) =>
        <dl class="entity business">
          <dt>Title</dt>
          <dd>{business.title.getOrElse("no title")}</dd>
          <dt>Description</dt>
          <dd>{business.description.getOrElse("no description")}</dd>
          <dt>Submitter</dt>
          <dd>{business.submittedBy.getOrElse("unknown")}</dd>
          <dt>Submission date</dt>
          <dd>{business.submissionDate.formatted("yyyy-dd-MM")}</dd>
        </dl>

      case None =>
        <dl class="empty entity business" />
    }
  }

  lazy val view = Group {
    <controls>
      { paging.view }
    </controls>

    <article>
      { self.renderIndex(renderEntityPreview) }

      { self.selectedEntity.map(renderEntity) }
    </article>

    <messages>
      { errors.map { _.toList.map { error => <error> { error.getMessage } </error> } } }
    </messages>
  }
}
