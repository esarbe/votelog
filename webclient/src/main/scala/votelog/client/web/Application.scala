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
import votelog.domain.politics.Person
import votelog.endpoint.client.{PersonReadOnlyStoreAjaxService, PersonStoreXhrEndpoint}

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}
import scala.xml.Node

object State {
  sealed trait Authenticated
  object Authenticated {
    object Unauthenticated extends Authenticated
    case class UserAuthenticated(user: User) extends Authenticated
  }
}

object Application {

  val context = Context("http://localhost:8080/api/v0", politics.Context(2019, politics.Language.English))
  val path = Var("")

  val personComponent = new PersonReadOnlyStoreAjaxService(context.url, context.context)
  val authService = new service.SessionServiceRest(context)
  val authComponent = new components.Authentication(authService)

  val languageComponent = new components.Language

  def main(args: Array[String]): Unit = {
    val qp = QueryParameters(context.context.language, 2019)

    val indexQueryParams = IndexQueryParameters(PageSize(100), Offset(0), qp)
    val personIndex: Rx[Seq[Person.Id]] =
      personComponent
        .index(indexQueryParams)
        .toRx
        .collect { case Some(Success(persons)) => persons }(Nil)


    val pview: Node =
      <section>
        <header>Users</header>
        <ul>
          { personIndex.map { ids => ids.map { id => <li>{id.value}</li> }} }
        </ul>
      </section>

    val auth: Element = dom.document.createElement("div")
    val persons: Element = dom.document.createElement("div")
    val language: Element = dom.document.createElement("div")
    val languageR: Element = dom.document.createElement("div")

    dom.document.body.appendChild(auth)
    dom.document.body.appendChild(language)
    dom.document.body.appendChild(persons)
    dom.document.body.appendChild(languageR)

    val n = <div>{languageComponent.model.map(_.show)}</div>

    mount(auth, authComponent.view)
    mount(language, languageComponent.view)
    mount(persons, pview)
    mount(languageR, n)
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
