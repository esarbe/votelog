package votelog.client.service

import cats.implicits._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import votelog.client.Context
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.SessionService.Error
import votelog.domain.authentication.SessionService.Error.{DecodingError, ServiceError}
import votelog.domain.authentication.{Authentication, SessionService, User}
import votelog.orphans.circe.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceRest(context: Context) extends SessionService[Future] {

  override def login(cred: Authentication.Credentials): Future[Either[Error, User]] = {
    cred match {
      case UserPassword(username, password) =>
        val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
        Ajax.post(
          url = context.url + "/auth/login",
          headers = Map("Authorization" -> basicAuthCreds)
        ).map { res =>
          if (200 <= res.status && res.status < 300) {
            println(s"json: ${res.responseText}")
            val user = io.circe.parser.decode[User](res.responseText)

            println(s"user: $user")

            user.leftMap(DecodingError)
          } else {
            Left(ServiceError(new Exception(s"unexpected status: ${res.status}")))
          }
        }.recover {
          case error => Left(ServiceError(error))
        }
    }
  }

  override def logout(): Future[Unit] = {
    Ajax
      .post(url = context.url + "/auth/login")
      .void
  }
}
