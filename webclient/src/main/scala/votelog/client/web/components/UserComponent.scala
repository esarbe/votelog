package votelog.client.web.components

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.implicits._
import mhtml.future.syntax._
import mhtml.implicits.cats._
import mhtml.{Rx, Var}
import votelog.client.web.components.html.tools.{ifEnter, inputPassword, inputText, set}
import votelog.domain.authentication.User
import votelog.domain.authentication.User.Permission
import votelog.domain.crudi.StoreAlg
import votelog.persistence.UserStore
import votelog.persistence.UserStore.{Password, Recipe}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Group}

class UserComponent(
  // need to figure out better way to put all of this together
  component: votelog.domain.authorization.Component,
  store: StoreAlg[Future, User, User.Id, Recipe],
  crud: CrudIndexComponent[User, User.Id, UserStore.Recipe]
) {

  def id(id: String): String = component.child(id).location

  object create {

    type Error = (String, String)

    private val name = Var("")
    private val email = Var("")
    private val password = Var("")
    private val confirmPassword = Var("")
    private val submit: Var[Unit] = Var(())

    private val validatedRecipe: Rx[Validated[NonEmptyList[Error], Recipe]] =
      for {
        name <- name
        email <- email
        password <- password
        confirmPassword <- confirmPassword
      } yield UserStore.validateRecipe(name, User.Email(email), Password.Clear(password), Password.Clear(confirmPassword))

    private val errors: Rx[List[(String, String)]] =
      validatedRecipe
        .map {
          case Invalid(errors) => errors.toList
          case _ => Nil
        }

    val model: Rx[Option[User.Id]] =
      validatedRecipe.sampleOn(submit)
        .flatMap {
          case Valid(recipe) => store.create(recipe).toRx
          case _ => Rx(None)
        }.map {
        case None => None
        case Some(Success(userId)) => Some(userId)
        case Some(Failure(error)) => println(error.getMessage); None
      }

    def id(id: String): String = component.child("create").child(id).location

    def form(legend: String): Elem = {
        <article class="user">
          <fieldset onkeyup={ ifEnter(set(submit)) }>
            <legend>{legend}</legend>
            { inputText(id = id("name"), label = "Name", rx = name, errors = errors) }
            { inputText(id = id("email"), label = "Email", rx = email, errors = errors) }
            { inputPassword(id = id("password"), label = "Password", rx = password, errors = errors) }
            { inputPassword(id = id("confirmPassword"), label = "Confirm password", rx = confirmPassword, errors = errors) }

            <input type="button" value="submit" onclick={ set(submit) } />
            { errors.map { _.map { case (key, message) =>
              <error> {key}: {message} </error>
                }
            }}
          </fieldset>
        </article>
    }
  }

  def renderPreviewUser(id: User.Id, user: User): Rx[Elem] = Rx {
    <article class="user" data-selected={ crud.selected.map(_.contains(id)) }>
      <dl>
        <dt>Name</dt>
        <dd>{user.name}</dd>
      </dl>
    </article>
  }

  def renderFullUser(user: Option[User]) = Rx {
    user match {
      case Some(user) =>
        <article class="user">
          <dl>
            <dd>Email</dd>
            <dt>{user.email.value}</dt>
          </dl>
          <dl>
            <dt>Name</dt>
            <dd>{user.name}</dd>
          </dl>
          <dl>
            <dt>Permissions</dt>
            <dd>
              { user.permissions.toList.map {
                  case Permission(capability, component) =>
                    <permission for={component.location}>{capability.toString}</permission>
                }
              }
            </dd>
          </dl>
        </article>

      case None =>
        <article class="user loading" />
    }
  }

  object index {
    val model = crud.ids
    val view = crud.view(renderPreviewUser)
  }

  object read {
    val model =  crud.selected
    val view = crud.model.map(renderFullUser)
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
