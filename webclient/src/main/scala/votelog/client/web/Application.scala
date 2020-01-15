package votelog.client.web

import mhtml.{Rx, Var}
import org.scalajs.dom
import org.scalajs.dom.HashChangeEvent
import votelog.client.Configuration
import votelog.client.service.{BusinessStoreXhr, NgoStoreXhr, PersonStoreXhr, SessionServiceXhr, UserStoreXhr}
import votelog.client.web.components.Authentication.State.{Authenticated, Unauthenticated}
import votelog.client.web.components.business.BusinessComponent
import votelog.client.web.components.{CrudIndexComponent, Paging, UserComponent}
import votelog.client.web.components.ngo.{NgoComponent, NgoIndexComponent}
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics
import votelog.domain.politics.{Business, Context, LegislativePeriod, Ngo}

import scala.xml.{Group, Node}

object Application {
  val RxUnit = Rx(Unit)
  val paging = Paging.Configuration()
  val url: Rx[String] = {
    val rx = Var(dom.window.location.href)
    val listener = (e: HashChangeEvent) => rx := e.newURL
    dom.window.addEventListener("hashchange", listener)
    rx
  }

  val location = url.map(_.dropWhile(_ != '#').drop(1)).dropRepeats

  val defaultContext = Context(LegislativePeriod.Default.id, politics.Language.English)
  val context: Var[Context] = Var(defaultContext)
  val configuration = Configuration("http://localhost:8080/api/v0")
  val root = Component.Root

  val authService = new SessionServiceXhr(configuration)

  val personsStore = new PersonStoreXhr(configuration)
  val userStore = new UserStoreXhr(configuration)
  val ngoStore = new NgoStoreXhr(configuration)
  val businessStore = new BusinessStoreXhr(configuration)

  val authComponent = new components.Authentication(authService)
  val languageComponent = new components.Language
  val personsComponent = new components.Persons(personsStore, context, paging.defaultPageSize)

  val ngoComponent = {
    val configuration = NgoComponent.Configuration(defaultContext, paging.defaultPageSize, paging.pageSizes)
    new NgoComponent(root.child("ngo"), configuration, ngoStore)
  }

  val userComponent = {
    val configuration = UserComponent.Configuration(paging.defaultPageSize, paging.pageSizes)
    new components.UserComponent(root.child("user"), configuration, userStore)
  }

  val businessComponent = {
    val configuration = BusinessComponent.Configuration(defaultContext, paging.defaultPageSize, paging.pageSizes)
    new components.business.BusinessComponent(root.child("business"), configuration, businessStore)
  }

  val appView: Rx[Node] = location.map(Router.apply)

  object Router {

    // TODO: mabye reuse http4s dsl path for 'location's
    def apply(location: String): Node = {
      location.drop(1).split('/').toList match {
        case "user" :: Nil =>
          userComponent.view
        case "user" :: id :: Nil =>
          userComponent.read.model := Some(User.Id(id))
          userComponent.index.view

        case "ngo" :: Nil =>
          ngoComponent.index.view
        case "ngo" :: id :: Nil =>
          ngoComponent.read.model := Some(Ngo.Id(id))
          ngoComponent.index.view

        case "session" :: Nil => authComponent.view
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
            <a href="#/person">Persons</a>
            <a href="#/ngo">NGOs</a>
            <a href="#/user">Users</a>
          </navigation>
          <user>
            { authComponent.model.map {
                case Authenticated(user) => <a href="#/session">Logout {user.name} </a>
                case Unauthenticated => <span> <a href="#/session">Login</a> or <a href="#/signup">Sign up</a> </span>
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
          Contact | Blag  gitlab
        </footer>
      </application>

    mhtml.mount(dom.document.body, content)
  }

}
