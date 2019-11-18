package votelog.client.service

import cats.implicits._
import io.circe.parser
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import votelog.client.Context
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.SessionService.Error
import votelog.domain.authentication.SessionService.Error.{AuthenticationFailed, DecodingError, ServiceError}
import votelog.domain.authentication.{Authentication, SessionService, User}
import votelog.orphans.circe.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceRest(context: Context) extends SessionService[Future] {

  override def login(cred: Authentication.Credentials): Future[Either[Error, User]] = {
    cred match {
      case UserPassword(username, password) =>
        val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
        Ajax
          .post(
            url = context.url + "/auth/login",
            headers = Map("Authorization" -> basicAuthCreds),
            withCredentials = true,
          )
          .map { res =>
            val user = parser.decode[User](res.responseText)
            user.leftMap(DecodingError)
          }.recover {
            case AjaxException(xhr) if  xhr.status == 401 =>
              Left(AuthenticationFailed)
            case error =>
              Left(ServiceError(error))
          }
    }
  }

  override def logout(): Future[Unit] = {
    Ajax
      .post(url = context.url + "/auth/login")
      .void
  }
}
