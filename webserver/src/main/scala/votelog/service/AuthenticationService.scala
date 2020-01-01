package votelog.service

import cats.Traverse
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRequest, AuthedRoutes, AuthedService, Request, Response, headers}
import org.reactormonk.CryptoBits
import votelog.domain.authentication.User
import votelog.persistence.UserStore

class AuthenticationService(
  userStore: UserStore[IO],
  crypto: CryptoBits,
) {

  val cookie = "authcookie"

  val retrieveUser: Kleisli[IO, String, Either[String, User]] =
    Kleisli(userStore.findByName(_).map(_.toRight("unknown username")))

  val authUser: Kleisli[IO, Request[IO], Either[String, User]] = Kleisli({ request =>
    val maybeUsername: Either[String, String] = for {
      header <- headers.Cookie.from(request.headers).toRight(s"Could not parse cookies header")
      cookie <- header.values.toList.find(_.name == cookie).toRight(s"No authentication cookie '$cookie' found")
      token <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
      username <- Either.catchOnly[NumberFormatException](token).leftMap(_.getMessage)
    } yield username

     maybeUsername.flatTraverse(retrieveUser.run)
  })

  val onFailure: AuthedRoutes[String, IO] =
    AuthedRoutes(req => OptionT.liftF(Forbidden(req.authInfo)))

  val middleware = AuthMiddleware(authUser, onFailure)
}
