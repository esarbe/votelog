package votelog.service

import cats._
import cats.implicits._

import scala.concurrent.duration.MILLISECONDS
import cats.effect.{Clock, IO, Sync}
import org.http4s.{AuthedService, BasicCredentials, HttpRoutes, HttpService, ResponseCookie}
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.reactormonk.CryptoBits
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authorization.{Capability, User}
import votelog.persistence.UserStore

class SessionService(
  crypto: CryptoBits,
  clock: Clock[IO],
) {

  val service: AuthedService[User, IO] = AuthedService {
    case POST -> Root / "login" as user  =>
      clock
        .realTime(MILLISECONDS)
        .flatMap { millis =>
          val message = crypto.signToken(user.name, millis.toString)
          Ok("Logged in!").map(_.addCookie(ResponseCookie("authcookie", message)))
        }
  }

}
