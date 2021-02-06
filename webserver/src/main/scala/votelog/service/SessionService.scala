package votelog.service

import cats.effect.{Clock, IO}
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.{AuthedRoutes, ResponseCookie, SameSite}
import org.reactormonk.CryptoBits
import votelog.domain.authentication.User
import votelog.domain.authorization.Component
import votelog.service.SessionService.CookieName
import votelog.orphans.circe.implicits._

import scala.concurrent.duration.MILLISECONDS

class SessionService(
  crypto: CryptoBits,
  clock: Clock[IO],
  component: Component
) {
  val service: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root as user => Ok(user.asJson)
    case POST -> Root as user =>
      clock
        .realTime(MILLISECONDS)
        .flatMap { millis =>
          val message = crypto.signToken(user.name, millis.toString)
          Ok(user.asJson).map(_.addCookie(
            ResponseCookie(
              name = CookieName,
              sameSite = SameSite.None,
              content = message,
              path = Some(component.location)
            ))
          )
        }

    case DELETE -> Root as user =>
      Ok(s"Logged out user ${user.name}")
        .map(_.addCookie(ResponseCookie(
          name = CookieName,
          content = "",
          sameSite = SameSite.None,
          path = Some(component.location)
        )))
  }
}

object SessionService {
  val CookieName: String = "authcookie"
}