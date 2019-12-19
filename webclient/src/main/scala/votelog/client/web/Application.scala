package votelog.client.web

import mhtml.Var
import org.scalajs.dom
import votelog.client.Configuration
import votelog.client.service.SessionServiceRest
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics
import votelog.domain.politics.{Context, LegislativePeriod}
import votelog.endpoint.client.PersonReadOnlyStoreAjaxService
import votelog.client.mhtml.mount.xmlElementEmbeddableNodeBuffer

object State {
  sealed trait Authenticated
  object Authenticated {
    object Unauthenticated extends Authenticated
    case class UserAuthenticated(user: User) extends Authenticated
  }
}

object Application {
  val defaultPageSize = PageSize(20)
  val context: Var[Context] = Var(Context(LegislativePeriod.Default.id, politics.Language.English))
  val configuration = Configuration("https://votelog.herokuapp.com/api/v0")
  //val configuration = Configuration("http://localhost:8080/api/v0")

  val personsService = new PersonReadOnlyStoreAjaxService(configuration)
  val authService = new SessionServiceRest(configuration)
  val authComponent = new components.Authentication(authService)

  val languageComponent = new components.Language
  val personsComponent = new components.Persons(personsService, context, defaultPageSize)

  def main(args: Array[String]): Unit = {
    val content =
      <application>
        <header>
          <section id="locations">
            <a href="#authentication">Login</a>
            <a href="#signup">Signup</a>
            <a href="#persons">Persons</a>
            <a href="#ngos">NGOs</a>
          </section>

          <section id="language">
            { languageComponent.view }
          </section>
        </header>

        <article>
          <section id="persons">
            { personsComponent.view }
          </section>

          <section id ="authentication">
            { authComponent.view }
          </section>

        </article>

        <footer>
        </footer>
      </application>

    votelog.client.mhtml.mount(dom.document.body, content)
  }
}
