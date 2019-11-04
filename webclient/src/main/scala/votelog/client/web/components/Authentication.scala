package votelog.client.web.components

import cats._
import mhtml.implicits.cats._
import cats.implicits._
import mhtml._
import mhtml.future.syntax._
import votelog.client.web.Application.debounce
import votelog.client.web.State
import votelog.client.web.State.Authenticated
import votelog.client.web.State.Authenticated.{Unauthenticated, UserAuthenticated}
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.AuthenticationAlg

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.{Elem, Node}


class Authentication(
  auth: AuthenticationAlg[Future],
) extends Component[State.Authenticated] {

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

  val state: Rx[Authenticated] =
    request.flatMap {
      case None => Rx(Unauthenticated)
      case Some(UserPassword(username, password)) =>
        auth
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


  def set[T](value: Var[T]): js.Dynamic => Unit = {
    event =>
      value.update(_ => event.target.value.asInstanceOf[T])
  }


  def setupView(
    authenticated: Rx[Authenticated],
    request: Rx[Option[UserPassword]],
    submit: Var[Unit],
    password: Var[String],
    username: Var[String],
  ): Rx[Elem] = {

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

          <input type="button" text="submit" onclick={ set(submit) } />

        </fieldset>

      case UserAuthenticated(user) =>
        <span>logged in</span>
    }
  }

  val view: Node =
    <div>{setupView(state, request, submit, password, username)}</div>

}
