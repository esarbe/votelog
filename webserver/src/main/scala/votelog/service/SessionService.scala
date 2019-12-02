package votelog.service

import cats.effect.{Clock, IO}
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{AuthedRoutes, ResponseCookie}
import org.reactormonk.CryptoBits
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.service.SessionService.CookieName

import scala.concurrent.duration.MILLISECONDS

class SessionService(
  crypto: CryptoBits,
  clock: Clock[IO],
  component: Component
) {

  val service: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case POST -> Root / "login" as user =>
      clock
        .realTime(MILLISECONDS)
        .flatMap { millis =>
          val message = crypto.signToken(user.name, millis.toString)
          Ok(user.asJson).map(_.addCookie(
            ResponseCookie(
              name = CookieName,
              content = message,
              path = Some(component.location)
            ))
          )
        }

    case POST -> Root / "logout" as _ =>
      Ok("Logged out.").map(_.removeCookie(CookieName))
  }
}

object SessionService {
  val CookieName: String = "authcookie"
}