package votelog.client.service

import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.circe
import io.circe.KeyDecoder
import io.circe.generic.semiauto.deriveDecoder
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import votelog.client.Context
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.SessionService.Error
import votelog.domain.authentication.SessionService.Error.ServiceError
import votelog.domain.authentication.{Authentication, SessionService, User}
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.politics.Person

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceRest(context: Context) extends SessionService[Future] {

  implicit val userIdCirceKeyDecoder: circe.Decoder[User.Id] = FUUID.fromString(_).toOption.map(User.Id)

  override def login(cred: Authentication.Credentials): Future[Either[Error, User.Id]] = {
    cred match {
      case UserPassword(username, password) =>
        val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
        Ajax.post(
          url = context.url + "/auth/login",
          headers = Map("Authorization" -> basicAuthCreds)
        ).map { res =>
          if (200 <= res.status && res.status < 300) {
            io.circe.parser.decode[User.Id](res.responseText)
              .leftMap(error => ServiceError(error))
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
