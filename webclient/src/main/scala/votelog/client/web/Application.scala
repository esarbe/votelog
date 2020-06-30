package votelog.client.web

import mhtml.{Rx, Var}
import org.scalajs.dom
import org.scalajs.dom.HashChangeEvent
import votelog.client.Configuration
import votelog.client.service.{BusinessStoreXhr, NgoStoreXhr, PersonStoreXhr, SessionServiceXhr, UserStoreXhr}
import votelog.client.web.components.Authentication.State.{Authenticated, Unauthenticated}
import votelog.client.web.components.business.BusinessComponent
import votelog.client.web.components.{CrudIndexComponent, Paging, UserComponent}
import votelog.client.web.components.ngo.NgoComponent
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.domain.politics
import votelog.domain.politics.{Business, Context, LegislativePeriod, Ngo, Person}

import scala.xml.{Group, Node}

object Application {
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

  val configuration = Configuration("https://votelog.herokuapp.com/api/v0")

  val root = Component.Root

  val authService = new SessionServiceXhr(configuration)
  val personsStore = new PersonStoreXhr(configuration)
  val userStore = new UserStoreXhr(configuration)
  val ngoStore = new NgoStoreXhr(configuration)
  val businessStore = new BusinessStoreXhr(configuration)

  val authComponent = new components.Authentication(authService)
  val languageComponent = new components.Language
  val personsComponent = new components.Persons(root.child("person"), personsStore, languageComponent.model)

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
    new components.business.BusinessComponent(root.child("business"), configuration, businessStore, languageComponent.model)
  }

  val appView: Rx[Node] = location.map(Router.apply).dropRepeats

  object Router {

    // TODO: maybe reuse http4s dsl path for 'location's
    def apply(location: String): Node = {
      location.drop(1).split('/').toList match {
        case "user" :: Nil =>
          userComponent.view
        case "user" :: id :: Nil =>
          userComponent.selectedId := Some(User.Id(id))
          userComponent.view

        case "ngo" :: Nil =>
          ngoComponent.view
        case "ngo" :: id :: Nil =>
          ngoComponent.selectedId := Some(Ngo.Id(id))
          ngoComponent.view

        case "business" :: Nil =>
          businessComponent.view
        case "business" :: id :: Nil =>
          businessComponent.selectedId := Some(Business.Id(id.toInt))
          businessComponent.view

        case "session" :: Nil => authComponent.view
        case "signup" :: Nil => userComponent.create.form("Sign Up")
        case "person" :: Nil => personsComponent.view
        case "person" :: id :: Nil =>
          personsComponent.maybeSelected := Some(Person.Id(id.toInt))
          personsComponent.view
        case a => <message>Not found</message>
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
            <a href="#/business">Businesses</a>
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

        { appView }

        <footer>
          Contact | Blag  gitlab
        </footer>
      </application>

    votelog.client.mhtml.mount(dom.document.body, content)
  }

}
