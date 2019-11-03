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
import scala.xml.Node

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

    val username: Var[String] = Var("")
    val password: Var[String] = Var("")
    val submit: Var[Unit] = Var(())

    val request: Rx[Option[UserPassword]] =
      username
        .flatMap { username =>
          password.map { password =>
            (username, password)
          }
        }
        .sampleOn(submit)
        .map { case (username, password) =>
          Some(UserPassword(username, password))
        }

    val authenticated: Rx[Authenticated] =
      request.flatMap {
        case None => Rx(Unauthenticated)
        case Some(UserPassword(username, password)) =>
          authenticationAlg
            .login(UserPassword(username,password))
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
      }


    def set[T](value: Var[T])(event: js.Dynamic): Unit =
      value.update(_ => event.target.value.asInstanceOf[T])

    def setRequest(
      request: Var[Option[UserPassword]],
      password: Var[String])(
      e: js.Dynamic) = {

      request.update(_ => Some(UserPassword("", "")))

    }

    def view(
      request: Rx[Option[UserPassword]],
      authenticated: Rx[Authenticated],
      submit: Var[Unit],
      password: Var[String],
      username: Var[String]
    ) = {

      authenticated.map {
        case Unauthenticated =>
          <fieldset>
            <legend>Login</legend>
            <dl>
              <dt>
                <label for="username">Username</label>
              </dt>
              <dd>
                <input id="username" type="text" value={username} oninput={debounce(200)(set(username))}/>
              </dd>
            </dl>
            <dl>
              <dt>
                <label for="password">Password</label>
              </dt>
              <dd>
                <input id="password" type="password" value={password} oninput={debounce(200)(set(password))}/>
              </dd>
            </dl>

            <input type="button" text="submit"
              onclick={ set(submit) _ }
            />

            {username}
            <br/>
            {password}
          </fieldset>

        case UserAuthenticated(user) =>
          <span>logged in</span>
        }
      }

    def rView =
      <div>{view(request, authenticated, submit, password, username)}</div>

    (password, rView)
  }



}
