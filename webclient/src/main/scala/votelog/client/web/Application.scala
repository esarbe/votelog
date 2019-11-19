package votelog.client
package web

import mhtml._
import mhtml.future.syntax._
import org.scalajs.dom
import org.scalajs.dom.raw.Element
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics
import votelog.domain.politics.Person
import votelog.endpoint.client.{PersonReadOnlyStoreService, PersonStoreXhrEndpoint}

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

  val personComponent = new PersonReadOnlyStoreService(context.url, context.context)
  val authService = new service.SessionServiceRest(context)
  val authComponent = new components.Authentication(authService)

  def main(args: Array[String]): Unit = {

    val indexQueryParams = IndexQueryParameters(PageSize(100), Offset(0), QueryParameters(context.context.language.iso639_1))
    val personIndex: Rx[Seq[Person.Id]] =
        authComponent.model
        .flatMap { _ =>
          personComponent
            .index(indexQueryParams)
            .toRx
            .collect { case Some(Success(persons)) => persons }(Nil)
      }

    val pview: Node =
      <div>
        <ul>
          {personIndex.map { id => <li>id</li> }}
        </ul>
      </div>


    val auth: Element = dom.document.createElement("div")
    val persons: Element = dom.document.createElement("div")
    dom.document.body.appendChild(auth)
    mount(auth, authComponent.view)
    mount(persons, pview)
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
