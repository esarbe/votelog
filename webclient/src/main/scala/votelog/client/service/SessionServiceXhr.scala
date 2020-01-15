package votelog.client.service

import cats.implicits._
import io.circe.parser
import io.circe.generic.auto._
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import votelog.client.Configuration
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.SessionService.Error
import votelog.domain.authentication.SessionService.Error.{AuthenticationFailed, DecodingError, ServiceError}
import votelog.domain.authentication.{Authentication, SessionService, User}
import votelog.orphans.circe.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceXhr(context: Configuration) extends SessionService[Future] {

  override def login(cred: Authentication.Credentials): Future[Either[Error, User]] = {
    cred match {
      case UserPassword(username, password) =>
        val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
        Ajax
          .post(
            url = context.url + "/auth/session",
            headers = Map("Authorization" -> basicAuthCreds),
            withCredentials = true,
          )
          .map { res =>
            parser.decode[User](res.responseText).leftMap(DecodingError)
          }.recover {
            case AjaxException(xhr) if  xhr.status == 401 =>
              Left(AuthenticationFailed)
            case error =>
              Left(ServiceError(error))
          }
    }
  }

  override def get: Future[Either[SessionService.Error, User]] = {
    Ajax.get(url = context.url + "/auth/user", withCredentials = true)
      .map { res =>
        parser.decode[User](res.responseText).leftMap(DecodingError)
      }
  }

  override def logout(): Future[Unit] = {
    Ajax
      .delete(url = context.url + "/auth/user", withCredentials = true)
      .void
  }
}
