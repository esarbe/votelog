package votelog.client.web.components

import mhtml._
import mhtml.future.syntax._
import votelog.client.web.components.Authentication.{Event, State}
import votelog.client.web.components.Authentication.Event.{Initialized, LoginFailed, LoginSucceeded, LogoutSucceeded, SubmitLogin, SubmitLogout}
import votelog.client.web.components.Authentication.State.{Authenticated, Unauthenticated}
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.{SessionService, User}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, Node}
import votelog.client.web.components.html.tools._
import votelog.domain.authentication.Authentication.Credentials

import scala.scalajs.js
class Authentication(
  auth: SessionService[Future],
) extends Component[Authentication.State] {

  val event: Var[Event] = Var(Initialized)

  def handleAuthEvent: Option[Try[Either[SessionService.Error, User]]] => Unit = {
    case Some(Success(Right(user))) => event := LoginSucceeded(user)
    case Some(Failure(error)) => event := LoginFailed(error)
    case Some(Success(Left(error))) => event := LoginFailed(error)
    case _ =>
  }

  private def logout(user: User): Unit = {
    event := SubmitLogout(user: User)
    auth.logout().toRx.impure.run {
      case Some(Success(())) => event := LogoutSucceeded
      case _ =>
    }
    ()
  }

  private def login(credentials: Credentials): Unit = {
    event := SubmitLogin(credentials)
    auth.login(credentials).toRx.impure.run(handleAuthEvent)
    ()
  }

  // check initial authentication
  auth.get.toRx.impure.run(handleAuthEvent)

  val username: Var[String] = Var("")
  val password: Var[String] = Var("")

  val loginRequest: Rx[Option[UserPassword]] =
    for {
      username <- username
      password <- password
    } yield
      if (username.nonEmpty && password.nonEmpty) Some(UserPassword(username, password))
      else None


  val model: Rx[State] = event.map {
    case Initialized => Unauthenticated
    case SubmitLogout(user) => Unauthenticated
    case SubmitLogin(credentials) => Unauthenticated
    case LoginFailed(reason) => Unauthenticated
    case LoginSucceeded(user) => Authenticated(user)
    case LogoutSucceeded => Unauthenticated
  }

  def view: Elem = {
    val cssClass = model.map {
      case Unauthenticated => "unauthenticated"
      case Authenticated(_) => "authenticated"
    }

    val disabled: Rx[Boolean] =
      model.collect {
        case _: Authenticated => true
        case Unauthenticated => false
      }(false)

    <fieldset class={cssClass}
      onkeyup={
        loginRequest.map {
          case Some(credentials) => ifEnter( _ => login(credentials))
          case None => (_: js.Dynamic) => ()
        }}>
      <legend>Login</legend>
        <dl>
          <dt>
            <label for="username">Username</label>
          </dt>
          <dd>
            <input id="username" type="text" disabled={disabled} value={ username }  onchange={ debounce(200)(set(username)) } />
          </dd>
        </dl>
        <dl>
          <dt>
            <label for="password">Password</label>
          </dt>
          <dd>
            <input id="password" type="password" disabled={disabled} value={ password } onchange={ debounce(200)(set(password)) } />
          </dd>
        </dl>
      {  model.map {
        case Authenticated(user) =>
          <input type="button" value="logout" onclick={ () => logout(user)} />
        case Unauthenticated =>
          <input type="button" value="login"
            onclick={ loginRequest.map {
              case Some(credentials) => () => login(credentials)
              case None => () => ()
            }} />
        }
      }

    </fieldset>
    }
}

object Authentication {
  sealed trait Event
  object Event {
    case object Initialized extends Event
    case class SubmitLogout(user: User) extends Event
    case class SubmitLogin(credentials: Credentials) extends Event
    case class LoginFailed(error: Throwable) extends Event
    case class LoginSucceeded(user: User) extends Event
    case object LogoutSucceeded extends Event
  }

  sealed trait State
  object State {
    case object Unauthenticated extends State
    case class Authenticated(user: User) extends State
  }
}