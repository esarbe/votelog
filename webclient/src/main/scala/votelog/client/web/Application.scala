package votelog.client
package web

import mhtml._
import mhtml.future.syntax._
import org.scalajs.dom
import cats.implicits._
import org.scalajs.dom.raw.Element
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics
import votelog.domain.politics.{Context, LegislativePeriod, Person}
import votelog.endpoint.client.{PersonReadOnlyStoreAjaxService, PersonStoreXhrEndpoint}

import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}
import immutable.Seq
import scala.xml.Node

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
  val path = Var("")

  val personsService = new PersonReadOnlyStoreAjaxService(configuration)
  val authService = new service.SessionServiceRest(configuration)
  val authComponent = new components.Authentication(authService)

  val languageComponent = new components.Language
  val personsComponent = new components.Persons(personsService, context, defaultPageSize)


  def main(args: Array[String]): Unit = {

    val content =
      <div>
        <header>
          <a href="#/authentication">Login</a>
          <a href="#/signup">Login</a>
          <a href="#/persons">Login</a>

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
      </div>

    val contentElement = dom.document.createElement("div")
    dom.document.body.appendChild(contentElement)
    mount(contentElement, content)

    println("ready.")
  }

  object debounce {
    var timeoutHandler: js.UndefOr[SetTimeoutHandle] = js.undefined
    def apply[A, B](timeout: Double)(f: A => B): A => Unit = { a =>
      timeoutHandler foreach js.timers.clearTimeout
      timeoutHandler = js.timers.setTimeout(timeout) {
        f(a)
        ()
      }
    }
  }

}
