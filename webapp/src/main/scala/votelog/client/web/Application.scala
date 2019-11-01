package votelog.client.web

import mhtml.implicits.cats._
import cats.implicits._
import cats.effect.IO

import org.scalajs.dom
import dom.document

import mhtml.future.syntax._
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Element
import scalatags.Text
import scalatags.Text.all._
import votelog.client.web.State.Authenticated
import votelog.client.web.State.Authenticated.{Unauthenticated, UserAuthenticated}
import votelog.domain.authentication.{Authentication, User}
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.User.Permission

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.util.{Failure, Success}
import votelog.client.web.State.Authenticated.Unauthenticated
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
    val (authenticated, authView) = authenticatedState(auth)

    val div: Element = dom.document.createElement("div")

    dom.document.body.appendChild(div)
    mount(div, authView)
    println("shup")

  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }


  case class Context(url: String, lang: String, year: Int)

  val context = Context("http://localhost:8000", "en", 2019)

  object auth extends AuthenticationAlg[Future] {
    override def login(cred: Authentication.Credentials): Future[Either[String, User]] = {
      cred match {
        case UserPassword(username, password) =>
          val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
          Ajax.post(
            url = context.url + "/auth/login",
            headers = Map("Authorization" -> basicAuthCreds)
          ).map { res =>
            if ( 200 <= res.status && res.status < 300)
              Right(User("Carl", User.Email("foo@bar.qux"), "", Set.empty))
            else
              Left("unatuhkajsdlkjadsf")
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



  def authenticatedState(authenticationAlg: AuthenticationAlg[Future]) = {

    val request: Var[Option[UserPassword]] = Var(None)

    /*
    val authenticated =
      request.flatMap {
        case None => Rx(Unauthenticated)
        case Some(UserPassword(username, password)) =>
          authenticationAlg
            .login(UserPassword(username, password))
            .toRx
            .map {
              case Some(Success(Right(user))) =>
                UserAuthenticated(user)
              case Some(Success(Left(message))) =>
                println(s"login failed: $message")
                Unauthenticated
              case Some(Failure(error)) =>
                println(s"error occured: $error")
                Unauthenticated
              case None =>
                Unauthenticated
            }
      }*/

    val username: Var[String] = Var("")
    val password: Var[String] = Var("")

    def updateUsername(event: js.Dynamic): Unit =
      username := event.target.value.asInstanceOf[String]

    def updatePassword(event: js.Dynamic): Unit = {
      println("updating password")
      password := event.target.value.asInstanceOf[String]
    }


    def submit(event: js.Dynamic): Unit = {
      println("foo, clicked!")
      username := "Bernie"
      ()

    }

    /*val view =
      (authenticated, username, password).mapN { case (auth, username, password) =>
        auth match {
          case Unauthenticated =>
            <fieldset>
              <legend>Login</legend>
              <dl>
                <dt><label for="name">Username</label></dt>
                <dd>
                  <input id="username"  type="text" oninput={debounce(200)(updateUsername)} />
                </dd>
              </dl>
              <dl>
                <dt><label for="password">Password</label></dt>
                <dd>
                  <input id="password" type="password" oninput={debounce(200)(updatePassword)}/>
                </dd>
              </dl>

              <input type="button" text="submit" />
            </fieldset>

          case UserAuthenticated(user) =>
            <span>logged in</span>
        }
      }
    */

    val view =
      (username: Rx[String], password: Rx[String]).mapN { case (username, password) =>
        <fieldset>
          <legend>Login</legend>
          <dl>
            <dt><label for="name">Username</label></dt>
            <dd>
              <input id="username"  type="text" oninput={debounce(200)(updateUsername)} />
            </dd>
          </dl>
          <dl>
            <dt><label for="password">Password</label></dt>
            <dd>
              <input id="password" type="password" oninput={debounce(200)(updatePassword)}/>
            </dd>
          </dl>
        </fieldset>
      }


    (password, view)
  }



}
