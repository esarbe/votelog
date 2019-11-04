package votelog.client.web

import cats._
import cats.Invariant
import mhtml.implicits.cats._
import cats.implicits._
import org.scalajs.dom
import dom.document
import mhtml.future.syntax._
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Element
import votelog.client.Context
import votelog.client.web.State.Authenticated
import votelog.client.web.State.Authenticated.{Unauthenticated, UserAuthenticated}
import votelog.domain.authentication.{Authentication, User}
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.User.Permission

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import votelog.domain.authentication.{AuthenticationAlg, User}

import scala.concurrent.ExecutionContext.Implicits.global

object State {
  trait Authenticated
  object Authenticated {
    object Unauthenticated extends Authenticated
    case class UserAuthenticated(user: User) extends Authenticated
  }
}

object Application {
  def main(args: Array[String]): Unit = {

    println("hello, worlds!")
    val authentication = new components.Authentication(auth)

    val div: Element = dom.document.createElement("div")

    dom.document.body.appendChild(div)
    mount(div, authentication.view)
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }


  val context = Context("http://localhost:8080/api/v0", "en", 2019)

  object auth extends AuthenticationAlg[Future] {

    override def login(cred: Authentication.Credentials): Future[Either[String, User]] = {
      cred match {
        case UserPassword(username, password) =>
          val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
          Ajax.post(
            url = context.url + "/auth/login",
            headers = Map("Authorization" -> basicAuthCreds)
          ).map { res =>
            if (200 <= res.status && res.status < 300)
              Right(User("Carl", User.Email("foo@bar.qux"), "", Set.empty))
            else
              Left(res.responseText)
          }
      }
    }
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
