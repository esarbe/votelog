package votelog.client.web

import mhtml.{Cancelable, Rx, Var}
import org.scalajs.dom
import org.scalajs.dom.HashChangeEvent
import votelog.client.Configuration
import votelog.client.service.{SessionServiceRest, UserStoreRest}
import votelog.client.web.State.Authenticated.{Unauthenticated, UserAuthenticated}
import votelog.client.web.components.html.tools.set
import votelog.client.web.components.{CrudIndexComponent, UserIndexComponent}
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics
import votelog.domain.politics.{Context, LegislativePeriod}
import votelog.endpoint.client.PersonReadOnlyStoreAjaxService

import scala.scalajs.js
import scala.xml.{Group, Node}

object State {
  sealed trait Authenticated
  object Authenticated {
    object Unauthenticated extends Authenticated
    case class UserAuthenticated(user: User) extends Authenticated
  }
}

object Application {
  val RxUnit = Rx(Unit)
  val defaultPageSize = PageSize(20)
  val url: Rx[String] = Var.create[String]("")({ rx: Var[String] =>
    val listener = (e: HashChangeEvent) => rx := e.newURL
    dom.window.addEventListener("hashchange", listener)
    Cancelable( () => dom.window.removeEventListener("hashchange", listener))
  })

  val location =
    url.map(_.dropWhile(_ != '#').drop(1)).dropRepeats

  val context: Var[Context] = Var(Context(LegislativePeriod.Default.id, politics.Language.English))
  val configuration = Configuration("https://votelog.herokuapp.com/api/v0")
  val root = Component.Root

  val personsService = new PersonReadOnlyStoreAjaxService(configuration)
  val authService = new SessionServiceRest(configuration)

  val userService = new UserStoreRest(configuration)
  val userIndexComponent = new UserIndexComponent(userService, defaultPageSize)

  val authComponent = new components.Authentication(authService)
  val languageComponent = new components.Language
  val personsComponent = new components.Persons(personsService, context, defaultPageSize)
  val userComponent = new components.UserComponent(root.child("user"), userService, userIndexComponent)
  val appView: Rx[Node] = location.map(Router.apply)

  object Router {

    // TODO: mabye reuse http4s dsl path for 'location's
    def apply(location: String): Node = {
      location.drop(1).split('/').toList match {
        case "user" :: Nil =>
          userComponent.view
        case "user" :: id :: Nil =>
          userComponent.read.model := Some(User.Id(id))
          userComponent.view
        case "login" :: Nil => authComponent.view
        case "signup" :: Nil => userComponent.create.form("Sign Up")
        case "person" :: Nil => personsComponent.view
        case a => Group(Nil)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val content =
      <application>
        <header>
          <branding>
            <logo />
            <name>VoteLog</name>
            <slogan>siehe selbst.</slogan>
          </branding>
          <navigation>
            <a href="#/signup">Signup</a>
            <a href="#/person">Persons</a>
            <a href="#/ngo">NGOs</a>
            <a href="#/user">Users</a>
          </navigation>
          <user>
            { authComponent.model.map {
                case UserAuthenticated(user) => <a href="#/logout">Logout {user.name} </a>
                case Unauthenticated => <a href="#/login">Login</a>
              }
            }
          </user>

          <settings>
            { languageComponent.view }
          </settings>
        </header>

        <content>
          { appView }
        </content>

        <footer>
        </footer>
      </application>

    votelog.client.mhtml.mount(dom.document.body, content)
  }

}
