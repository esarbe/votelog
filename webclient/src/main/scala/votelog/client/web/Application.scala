package votelog.client
package web

import mhtml._
import org.scalajs.dom
import org.scalajs.dom.raw.Element
import votelog.domain.authentication.User

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

object State {
  sealed trait Authenticated
  object Authenticated {
    object Unauthenticated extends Authenticated
    case class UserAuthenticated(user: User) extends Authenticated
  }
}

object Application {

  val context = Context("http://localhost:8080/api/v0", "en", 2019)

  val authService = new service.AuthenticationAjax(context)
  val authComponent = new components.Authentication(authService)

  def main(args: Array[String]): Unit = {

    val div: Element = dom.document.createElement("div")
    dom.document.body.appendChild(div)
    mount(div, authComponent.view)
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
