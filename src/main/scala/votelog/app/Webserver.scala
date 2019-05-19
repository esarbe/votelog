package votelog.app

import cats.effect._
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{BasicCredentials, HttpRoutes}
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import votelog.domain.authorization.Component.Root
import votelog.domain.authorization.{Capability, Component, User}
import votelog.implementation.Log4SLogger
import votelog.persistence.UserStore
import votelog.service._

object Webserver extends IOApp {

  implicit val log = new Log4SLogger[IO](org.log4s.getLogger)
  val loadConfiguration: IO[Configuration] = loadConfigF[IO, Configuration]("votelog.webapp")

  def run(args: List[String]): IO[ExitCode] =
    for {
      configuration <- loadConfiguration
      voteLog = VoteLog[IO](configuration)
      runServer <-
        voteLog.use { voteLog =>
          val routes = setupHttpRoutes(configuration.security, voteLog)

          setupAdmin(voteLog.user) *>
            runVotelogWebserver(configuration.http, routes)
        }
    } yield runServer

  private def setupAdmin(user: UserStore[IO]) =
    for {
      _ <- log.info("setting up initial admin account")
      id <- user.create(UserStore.Recipe("admin", User.Email("admin@votelog.ch"), "foo"))
      _ <- user.grantPermission(id, Component.Root, Capability.Create)
      _ <- user.grantPermission(id, Component.Root, Capability.Read)
      _ <- user.grantPermission(id, Component.Root, Capability.Update)
      _ <- user.grantPermission(id, Component.Root, Capability.Delete)
      _ <- log.info("admin account created")
      user <- user.findByName("admin")
      _ <- log.info(s"user: $user")
     } yield ()


  private def runVotelogWebserver(
    config: Configuration.Http,
    routes: HttpRoutes[IO],
  ): IO[ExitCode] =
    log.info(s"attempting to bind to port ${config.port}") *>
      BlazeServerBuilder[IO]
        .bindHttp(config.port, config.interface)
        .withHttpApp(routes.orNotFound)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)

  def setupHttpRoutes(
    configuration: Configuration.Security,
    votelog: VoteLog[IO]
  ): HttpRoutes[IO] = {

    val pws =
      new PoliticianService(
        component = Root.child("politician"),
        store = votelog.politician,
        voteAlg = votelog.vote,
        log = log,
        authAlg = votelog.authorization
      )

    val mws = new MotionService(Root.child("motion"), votelog.motion, votelog.authorization)
    val uws = new UserService(Root.child("user"), votelog.user, votelog.authorization)
    val nws = new NgoService(Root.child("ngo"), votelog.ngo, votelog.authorization)

    val key = PrivateKey(configuration.secret.getBytes)
    val crypto: CryptoBits = CryptoBits(key)
    val clock = Clock[IO]
    val auth = new AuthenticationService(votelog.user, crypto).middleware

    val validateCredentials: BasicCredentials => IO[Option[User]] = {
      creds =>
        for {
          maybeUser <- votelog.user.findByName(creds.username)
          hashedPassword <- votelog.passwordHasher.hashPassword(creds.password)
        } yield maybeUser.filter(_.hashedPassword == hashedPassword)
    }

    val basicAuth: AuthMiddleware[IO, User] = BasicAuth("votelog", validateCredentials)
    val session = new SessionService(crypto, clock)
    val apiRoot = "/api/v0"


    Router(
      s"$apiRoot/politician" -> auth(pws.service),
      s"$apiRoot/motion" -> auth(mws.service),
      s"$apiRoot/user" -> auth(uws.service),
      s"$apiRoot/ngo" -> auth(nws.service),
      s"$apiRoot/auth" -> basicAuth(session.service),
    )
  }
}
