package votelog.service

import cats.implicits._
import cats.effect.{IO, Sync}
import org.http4s.{AuthedService, BasicCredentials, HttpRoutes, HttpService}
import org.http4s.dsl.io.{->, /, GET, NotFound, Ok, POST, Root}
import org.http4s.server.middleware.authentication.BasicAuth
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authorization.{Capability, User}
import votelog.persistence.UserStore

class SessionService[F[_]: Sync](
  userStore: UserStore[F],
  passwordHasherAlg: PasswordHasherAlg[F]
) {


  val validateCredentials: BasicCredentials => F[Option[User]] = {
    creds =>
      for {
        maybeUser <- userStore.findByName(creds.username)
        hashedPassword = passwordHasherAlg.hashPassword(creds.password)
      } yield maybeUser.filter(_.hashedPassword == hashedPassword)
  }

  // inject BasicAuth instead of userStore
  val basicAuth = BasicAuth("votelog", validateCredentials)


  val voting: HttpRoutes[IO] =  HttpRoutes.of[IO] {
    case POST -> Root / "login" =>
      userStore.read(id)
  }

}
