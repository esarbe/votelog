package votelog.client.web

import mhtml.{Rx, Var}
import org.scalajs.dom
import org.scalajs.dom.{Element, HashChangeEvent, MutationObserverInit, raw}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{MutationObserver, MutationRecord}
import scalatags.JsDom
import scalatags.JsDom.Attr
import scalatags.generic.{Attr, AttrValue, Modifier}
import votelog.client.Configuration
import votelog.client.service.{BusinessStoreXhr, NgoStoreXhr, PersonStoreXhr, SessionServiceXhr, UserStoreXhr}
import votelog.client.web.components.Authentication.State.{Authenticated, Unauthenticated}
import votelog.client.web.components.business.BusinessComponent
import votelog.client.web.components.{CrudIndexComponent, Paging, UserComponent}
import votelog.client.web.components.ngo.NgoComponent
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.domain.politics
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Ngo, Person}

import scala.scalajs.js
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

  val configuration = Configuration("https://votelog.herokuapp.com/api/v0")
  //val configuration = Configuration("http://localhost:8080/api/v0")

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

    // TODO: mabye reuse http4s dsl path for 'location's
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

  implicit val langStringer: Stringer[Language] = (lang: Language) => lang.iso639_1

  trait Stringer[T] { def asString(t: T): String }

  implicit def varAttr[T: Stringer]: JsDom.GenericAttr[Var[T]] = new JsDom.GenericAttr[Var[T]]{
    override def apply(elem: dom.Element, a: JsDom.Attr, v: Var[T]): Unit = {
      val cancelable = v.impure.run { t =>
        elem.setAttribute(a.name, implicitly[Stringer[T]].asString(t))
      }
      new MutationObserver({(records: js.Array[MutationRecord], _) =>
        if (records.exists(record => record.removedNodes(0) != null && record.removedNodes(0) == elem))(cancelable.cancel)
      }).observe(dom.document.body, MutationObserverInit(childList = true))
    }
  }

  def rxAttr[T: Stringer]: AttrValue[Element, Rx[T]] = new AttrValue[dom.Element, Rx[T]] {
    def apply(elem: dom.Element, a: Attr, v: Rx[T]): Unit = {
      val cancelable = v.impure.run { (t: T) =>
        elem.asInstanceOf[js.Dynamic].updateDynamic(a.name)(implicitly[Stringer[T]].asString(t))
      }
      elem.addEventListener("remove", (e: dom.Event) =>  cancelable.cancel)
    }
  }


  import scalatags.JsDom.all._
  implicit val bigDecimalAttrValue: JsDom.GenericAttr[BigDecimal] = scalatags.JsDom.implicits.genericAttr[BigDecimal]

  val q: JsDom.TypedTag[Div] = div(
    id := "johnny",
    onclick := { () => println("foo") },
    `class` := languageComponent.model,
  )("asdf")

  input(value := "boa")

  def main(args: Array[String]): Unit = {
    val f = tag("application")(
              tag("header")(

              ))
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
    dom.document.body.appendChild(q.render)
  }

}
