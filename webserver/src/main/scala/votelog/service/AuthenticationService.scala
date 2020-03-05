package votelog.service

import cats.Traverse
import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.IO
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRequest, AuthedRoutes, AuthedService, Request, Response, headers}
import org.reactormonk.CryptoBits
import votelog.domain.authentication.User
import votelog.persistence.UserStore
import votelog.service.AuthenticationService.{Cookie, Error, FallbackUsername}
import votelog.service.AuthenticationService.Error.{CookieInvalid, NoAuthCookie, NoAuthHeader, UnknownUser}

class AuthenticationService(
  userStore: UserStore[IO],
  crypto: CryptoBits,
) {

  val retrieveUser: Kleisli[IO, String, Either[Error, User]] =
    Kleisli(name => userStore.findByName(name).map(_.toRight(UnknownUser(name))))

  val authUser: Kleisli[IO, Request[IO], Either[Error, User]] = Kleisli({ request =>
    val maybeUsername: Either[Error, String] = for {
      header <- headers.Cookie.from(request.headers).toRight(NoAuthHeader)
      cookie <- header.values.toList.find(_.name == Cookie).toRight(NoAuthCookie)
      token <- crypto.validateSignedToken(cookie.content).toRight(CookieInvalid)
      username <- Either.catchOnly[NumberFormatException](token).leftMap(_ => CookieInvalid)
    } yield username

    maybeUsername.flatTraverse(retrieveUser.run)
  })

  val onFailure: AuthedRoutes[String, IO] =
    AuthedRoutes(req => OptionT.liftF(Forbidden(req.authInfo)))

  val fetchGuest = userStore.findByName(FallbackUsername).map(_.toRight(UnknownUser(FallbackUsername)))

  val fallbackUser: Kleisli[IO, Either[Error, User], Either[Error, User]] = Kleisli {
    case Left(NoAuthHeader) => fetchGuest
    case Left(NoAuthCookie) => fetchGuest
    case otherwise => IO.pure(otherwise)
  }

  val mapErrors: Kleisli[IO, Either[Error, User], Either[String, User]] = Kleisli ({
    case Left(error) => IO.pure(Left(error.getMessage))
    case Right(user) => IO.pure(Right(user))
  })

  val authUserWithFallback: Kleisli[IO, Request[IO], Either[String, User]] =
    authUser andThen fallbackUser andThen mapErrors

  val middleware = AuthMiddleware(authUserWithFallback, onFailure)
}
 object AuthenticationService {
   val Cookie = "authcookie"
   val FallbackUsername = "guest"

   trait Error extends Throwable
   object Error {
     case object NoAuthHeader extends Error { override def getMessage = "No cookies in request header" }
     case object NoAuthCookie extends Error { override def getMessage = s"No cookie named '$Cookie' found in header"}
     case object CookieInvalid extends Error { override def getMessage = s"'$Cookie' has invalid signature"}
     case class UnknownUser(name: String) extends Error { override def getMessage = s"no user '$name' found "}
   }
 }