package votelog.client.web.components.ngo

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import mhtml.future.syntax._
import mhtml.{Rx, Var}
import votelog.client.web.components.{CrudIndexComponent, Paging}
import votelog.client.web.components.html.tools.{ifEnter, inputPassword, inputText, set}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics.{Context, Ngo}
import votelog.persistence.NgoStore
import votelog.persistence.NgoStore.Recipe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Group, Node}

object NgoComponent {
  case class Configuration(defaultContext: Context, defaultPageSize: PageSize, pageSizes: Seq[PageSize])
}

class NgoComponent(
  component: votelog.domain.authorization.Component,
  configuration: NgoComponent.Configuration,
  val store: NgoStore[Future],
) extends CrudIndexComponent[Ngo, Ngo.Id] { self =>

  val indexQueryParameters: Rx[store.IndexQueryParameters] = Rx(())
  val queryParameters: Rx[store.QueryParameters] = Rx(())
  val queryParametersView: Option[Node] = None

  def id(id: String): String = component.child(id).location

  object create {

    type Error = (String, String)

    private val name = Var("")
    private val submitCreate = Var("")

    private val recipe: Rx[Validated[NonEmptyList[(String, String)], Recipe]] =
      for {
        name <- name
      } yield NgoStore.validateRecipe(name)

    private val errors: Rx[List[(String, String)]] =
      recipe
        .map {
          case Invalid(errors) => errors.toList.map { case (field, message) => (id(field), message) }
          case _ => Nil
        }

    private val validatedRecipe =
      recipe.map {
        case Valid(recipe) => Some(recipe)
        case _ => None
      }

    val model: Rx[Option[Either[Throwable, Ngo.Id]]] =
      validatedRecipe.sampleOn(submitCreate)
        .flatMap {
          case Some(recipe) => store.create(recipe).toRx
          case None => Rx(None)
        }
        .map {
          case None => None
          case Some(Success(ngoId)) => Some(Right(ngoId))
          case Some(Failure(error)) => Some(Left(error))
      }

    def id(id: String): String = component.child("create").child(id).location

    def form(legend: String): Elem = {
      <article class="ngo">
        <fieldset onkeyup={ ifEnter(set(submitCreate)) }>
          <legend>{legend}</legend>
          { inputText(id = id("name"), label = "Name", rx = name, errors = errors) }

          <input type="button" value="create" onclick={ set(submitCreate) } disabled={ validatedRecipe.map(_.isEmpty) } />
          {
            model.map {
              case Some(Right(id)) => <p>Ngo {id.value} created</p>
              case Some(Left(error)) => <p>Error creating new Ngo: {error.getMessage}</p>
              case None => <p></p>
            }
          }
        </fieldset>
      </article>
    }
  }

  def renderNgoPreview(id: Ngo.Id, ngo: Ngo): Rx[Elem] = Rx {
    <article class="ngo preview" data-selected={ selectedId.map(_.contains(id)) }>
      <dl>
        <dt>Name</dt>
        <dd>{ngo.name}</dd>
      </dl>
    </article>
  }

  def renderFullNgo(ngo: Option[Ngo]): Elem = {
    ngo match {
      case Some(ngo) =>
        <article class="ngo">
          <dl>
            <dd>NGO</dd>
            <dt>{ngo.name}</dt>
          </dl>
        </article>

      case None =>
        <article class="ngo loading" />
    }
  }

  object index {

    val pagingConfiguration = Paging.Configuration(self.configuration.defaultPageSize, configuration.pageSizes)
    val paging: Paging = new Paging(self.component.child("paging"), pagingConfiguration)

    val view =
      <section>
        <controls>
          <fieldset>
            { paging.view }
          </fieldset>

        </controls>
        { self.renderIndex(renderNgoPreview) }
      </section>
  }

  object read {
    val model = self.selectedId
    val view: Rx[Elem] = self.selectedEntity.map(renderFullNgo)
  }

  val view = Group {
    <section id={id("index")} >
      { index.view }
    </section>
    <section id={id("create")}>
      { create.form("Create") }
    </section>
    <section id={id("read")} >
      { read.view }
    </section>
  }
}
