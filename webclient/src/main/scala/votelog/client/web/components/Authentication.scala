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
import votelog.domain.authentication.SessionService
import votelog.domain.authentication.SessionService.Error.{AuthenticationFailed, DecodingError, ServiceError}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.{Elem, Node}


class Authentication(
  auth: SessionService[Future],
) extends Component[State.Authenticated] {

  val username: Var[String] = Var("")
  val password: Var[String] = Var("")
  val submitLogin: Var[Unit] = Var(())

  val loginRequest: Rx[Option[UserPassword]] =
    username
      .flatMap { username =>
        password.map { password =>
          (username, password)
        }
      }
      .sampleOn(submitLogin)
      .map { case (username, password) =>
        Some(UserPassword(username, password))
      }

  val model: Rx[Authenticated] =
    loginRequest.flatMap {
      case None => Rx(Unauthenticated)
      case Some(UserPassword(username, password)) =>
        auth
          .login(UserPassword(username,password))
          .map {
            case Right(user) =>
              UserAuthenticated(user)
            case Left(ServiceError(source)) =>
              println(s"Error calling authentication service: $source")
              Unauthenticated
            case Left(AuthenticationFailed) =>
              println(s"Authentication failed")
              Unauthenticated
            case Left(DecodingError(source)) =>
              println(s"Error decoding response: $source")
              Unauthenticated
          }
          .toRx
          .collect { case Some(Success(user)) => user }(Unauthenticated)
    }


  def ifEnter(run: js.Dynamic => Unit): js.Dynamic => Unit = {
    event =>
      if (event.keyCode == 13) run(event)
  }

  def set[T](value: Var[T]): js.Dynamic => Unit = {
    event =>
      value.update(_ => event.target.value.asInstanceOf[T])
  }

  def loginView(
    request: Rx[Option[UserPassword]],
    submitLogin: Var[Unit],
    model: Rx[Authenticated],
    password: Var[String],
    username: Var[String],
  ): Elem = {
    val cssClass =
      model.map {
        case UserAuthenticated(_) => "authenticated"
        case Unauthenticated => "unauthenticated"
      }

    <fieldset id="authentication" class={cssClass} onkeyup={ ifEnter(set(submitLogin)) }>
      <legend>Login</legend>
      <dl>
        <dt>
          <label for="username">Username</label>
        </dt>
        <dd>
          <input id="username" type="text" value={ username }  oninput={ debounce(200)(set(username)) } />
        </dd>
      </dl>
      <dl>
        <dt>
          <label for="password">Password</label>
        </dt>
        <dd>
          <input id="password" type="password" value={ password } oninput={ debounce(200)(set(password)) } />
        </dd>
      </dl>

     <input type="button" value="login" onclick={ set(submitLogin) } />

    </fieldset>
  }

  val view: Node =
    loginView(loginRequest, submitLogin, model, password, username)

}
