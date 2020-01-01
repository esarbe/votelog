package votelog.client.web.components.ngo

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import mhtml.future.syntax._
import mhtml.{Rx, Var}
import votelog.client.web.components.CrudIndexComponent
import votelog.client.web.components.html.tools.{ifEnter, inputPassword, inputText, set}
import votelog.domain.crudi.StoreAlg
import votelog.domain.politics.Ngo
import votelog.persistence.NgoStore
import votelog.persistence.NgoStore.Recipe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Group}

class NgoComponent(
  // need to figure out better way to put all of this together
  component: votelog.domain.authorization.Component,
  store: StoreAlg[Future, Ngo, Ngo.Id, Recipe],
  crud: CrudIndexComponent[Ngo, Ngo.Id, NgoStore.Recipe]
) {

  def id(id: String): String = component.child(id).location

  object create {

    type Error = (String, String)

    private val name = Var("")
    private val email = Var("")
    private val password = Var("")
    private val confirmPassword = Var("")
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
        }.map {
          case None => None
          case Some(Success(ngoId)) => Some(Right(ngoId))
          case Some(Failure(error)) => Some(Left(error))
      }

    def id(id: String): String = component.child("create").child(id).location

    def form(legend: String): Elem = {
      <article class="Ngo">
        <fieldset onkeyup={ ifEnter(set(submitCreate)) }>
          <legend>{legend}</legend>
          { inputText(id = id("name"), label = "Name", rx = name, errors = errors) }
          { inputText(id = id("email"), label = "Email", rx = email, errors = errors) }
          { inputPassword(id = id("password"), label = "Password", rx = password, errors = errors) }
          { inputPassword(id = id("confirmPassword"), label = "Confirm password", rx = confirmPassword, errors = errors) }

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

  def renderPreviewNgo(id: Ngo.Id, ngo: Ngo): Rx[Elem] = Rx {
    <article class="ngo" data-selected={ crud.selected.map(_.contains(id)) }>
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
    val model = crud.ids
    val view = crud.view(renderPreviewNgo)
  }

  object read {
    val model =  crud.selected
    val view: Rx[Elem] = crud.model.map(renderFullNgo)
  }

  val view = Group {
    <section id={id("")} >
      { index.view }
    </section>
    <section id={id("read")} >
      { read.view }
    </section>
  }
}
