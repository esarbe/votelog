package votelog.service

import cats.effect.{Clock, IO}
import org.http4s.dsl.io._
import org.http4s.{AuthedService, ResponseCookie}
import org.reactormonk.CryptoBits
import votelog.domain.authorization.User

import scala.concurrent.duration.MILLISECONDS

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
