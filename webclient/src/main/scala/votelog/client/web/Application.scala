package votelog.client.web

import mhtml.{Rx, Var}
import org.scalajs.dom
import votelog.client.Configuration
import votelog.client.service.{SessionServiceRest, UserStoreRest}
import votelog.client.web.components.{CrudIndexComponent, UserIndexComponent}
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics
import votelog.domain.politics.{Context, LegislativePeriod}
import votelog.endpoint.client.PersonReadOnlyStoreAjaxService

import scala.xml.Node

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
  val context: Var[Context] = Var(Context(LegislativePeriod.Default.id, politics.Language.English))
  val configuration = Configuration("https://votelog.herokuapp.com/api/v0")
  val root = Component.Root
  //val configuration = Configuration("http://localhost:8080/api/v0")

  val personsService = new PersonReadOnlyStoreAjaxService(configuration)
  val authService = new SessionServiceRest(configuration)

  val userService = new UserStoreRest(configuration)
  val userIndexComponent = new UserIndexComponent(userService, defaultPageSize)

  val authComponent = new components.Authentication(authService)
  val languageComponent = new components.Language
  val personsComponent = new components.Persons(personsService, context, defaultPageSize)
  val userComponent = new components.UserComponent(root.child("user"), userService, userIndexComponent)

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
            <a href="#authentication">Login</a>
            <a href="#signup">Signup</a>
            <a href="#persons">Persons</a>
            <a href="#ngos">NGOs</a>
            <a href="#users">NGOs</a>
          </navigation>

          <settings>
            { languageComponent.view }
          </settings>
        </header>

        <components>
          <component id="persons">
            { personsComponent.view }
          </component>

          <component id ="authentication">
            { authComponent.view }
          </component>

          <component id ="users">
            { userComponent.view }
          </component>

        </components>

        <footer>
        </footer>
      </application>

    votelog.client.mhtml.mount(dom.document.body, content)
  }

}
