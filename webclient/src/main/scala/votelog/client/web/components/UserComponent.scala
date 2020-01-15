package votelog.client.web.components

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.implicits._
import mhtml.future.syntax._
import mhtml.implicits.cats._
import mhtml.{Rx, Var}
import votelog.client.web.components.html.tools.{ifEnter, inputPassword, inputText, set}
import votelog.domain.authentication.User
import votelog.domain.authentication.User.{Email, Permission}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.crudi.StoreAlg
import votelog.domain.politics.Context
import votelog.persistence.UserStore
import votelog.persistence.UserStore.{Password, Recipe}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Group, Node}

object UserComponent {
  case class Configuration(defaultPageSize: PageSize, pageSizes: Seq[PageSize])
}

class UserComponent(
  // need to figure out better way to put all of this together
  component: votelog.domain.authorization.Component,
  configuration: UserComponent.Configuration,
  val store: UserStore[Future],
) extends CrudIndexComponent[User, User.Id] { self =>

  val indexQueryParameters: Rx[store.IndexQueryParameters] = Rx(())
  val queryParameters: Rx[store.QueryParameters] = Rx(())
  val queryParametersView: Option[Node] = None


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
        email <- email
        password <- password
        confirmPassword <- confirmPassword
      } yield UserStore.validateRecipe(name, User.Email(email), Password.Clear(password), Password.Clear(confirmPassword))

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

    val model: Rx[Option[Either[Throwable, User.Id]]] =
      validatedRecipe.sampleOn(submitCreate)
        .flatMap {
          case Some(recipe) => store.create(recipe).toRx
          case None => Rx(None)
        }.map {
          case None => None
          case Some(Success(userId)) => Some(Right(userId))
          case Some(Failure(error)) => Some(Left(error))
      }

    def id(id: String): String = component.child("create").child(id).location

    def form(legend: String): Elem = {
      <article class="user">
        <fieldset onkeyup={ ifEnter(set(submitCreate)) }>
          <legend>{legend}</legend>
          { inputText(id = id("name"), label = "Name", rx = name, errors = errors) }
          { inputText(id = id("email"), label = "Email", rx = email, errors = errors) }
          { inputPassword(id = id("password"), label = "Password", rx = password, errors = errors) }
          { inputPassword(id = id("confirmPassword"), label = "Confirm password", rx = confirmPassword, errors = errors) }

          <input type="button" value="create" onclick={ set(submitCreate) } disabled={ validatedRecipe.map(_.isEmpty) } />
          {
            model.map {
              case Some(Right(id)) => <p>User {id.value} created</p>
              case Some(Left(error)) => <p>Error creating new user: {error.getMessage}</p>
              case None => <p></p>
            }
          }
        </fieldset>
      </article>
    }
  }

  def renderUserPreview(id: User.Id, user: User): Rx[Elem] = Rx {
    <article class="user" data-selected={ self.selectedId.map(_.contains(id)) }>
      <dl>
        <dt>Name</dt>
        <dd>{user.name}</dd>
      </dl>
    </article>
  }

  def renderFullUser(user: Option[User]): Elem = {
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

    val paging: Paging =
      new Paging {
        override val configuration: Paging.Configuration =
          Paging.Configuration(self.configuration.defaultPageSize, configuration.pageSizes)
        implicit val component: Component = self.component.child("paging")
      }

    val view =
      <section>
        <controls>
          <fieldset>
            { paging.view }
          </fieldset>

        </controls>
        { self.renderIndex(renderUserPreview) }
      </section>

    val model = self.ids
  }

  object read {
    val model =  self.selectedId
    val view: Rx[Elem] = self.selectedEntity.map(renderFullUser)
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
