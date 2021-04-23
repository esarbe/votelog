package votelog.client.web.components

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import mhtml.future.syntax._
import mhtml.{Rx, Var}
import votelog.client.web.components.html.tools.{ifEnter, inputPassword, inputText, set}
import votelog.domain.authentication.User
import votelog.domain.authentication.User.Permission
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction
import votelog.domain.data.Sorting.Direction.Descending
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
) extends CrudIndexComponent[User, User.Id, User.Partial, Unit, IndexQueryParameters[Unit, User.Field, User.Field]] { self =>

  val indexQueryParameters: Rx[IndexQueryParameters[Unit, User.Field, User.Field]] =
    Rx(IndexQueryParameters(
      pageSize = PageSize(10),
      offset = Offset(0),
      indexContext = (),
      orderings = List(User.Field.Name -> Descending),
      fields = User.Field.values.toSet))


  val queryParameters: Rx[Unit] = Rx(())
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
      <controls class="user">
        <fieldset class="create" onkeyup={ ifEnter(set(submitCreate)) }>
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
      </controls>
    }
  }

  def renderEntityPreview(id: User.Id, user: User): Elem =
    <article class="user" data-selected={ self.selectedId.map(_.contains(id)) }>
      <dl>
        <dt>Name</dt>
        <dd>{user.name}</dd>
      </dl>
    </article>

  def renderEntity(user: Option[User]): Elem = {
    user match {
      case Some(user) =>
        <section class="entity user">
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
        </section>

      case None =>
        <section class="entity user loading" />
    }
  }

  val errors: Rx[Iterable[Throwable]] = Rx(Nil)
  val paging = self.entitiesCount.map { totalEntities =>
    val pagingConfiguration = Paging.Configuration(self.configuration.defaultPageSize, configuration.pageSizes, totalEntities = totalEntities)
    new Paging(self.component.child("paging"), pagingConfiguration).view
  }

  lazy val view = Group {
    <controls>
      { paging }
    </controls>

    <article id={id("index")} >
      { self.renderIndex(renderEntityPreview) }
      { self.selectedEntity.map(renderEntity) }
    </article>

    <messages>
      { errors.map { _.toList.map { error => <error> { error.getMessage } </error> } } }
    </messages>
  }
}
