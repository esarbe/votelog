package votelog.service

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request, headers}
import org.reactormonk.{CryptoBits, PrivateKey}
import votelog.domain.authorization.User
import votelog.persistence.UserStore

class AuthenticationService(
  userStore: UserStore[IO],
  crypto: CryptoBits,
) {

  val retrieveUser: Kleisli[IO, User.Id, User] = Kleisli(userStore.read)

  val authUser: Kleisli[IO, Request[IO], Either[String, User]] = Kleisli({ request =>
    val maybeId: Either[String, User.Id] = for {
      header <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
      cookie <- header.values.toList.find(_.name == "authcookie").toRight("Couldn't find the authcookie")
      token <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
      message <- Either.catchOnly[NumberFormatException](User.Id(token.toLong)).leftMap(_.toString)
    } yield message
    maybeId.traverse(retrieveUser.run)
  })

  val onFailure: AuthedService[String, IO] =
    Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  val middleware = AuthMiddleware(authUser, onFailure)
}
