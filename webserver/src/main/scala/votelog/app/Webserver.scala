package votelog.app

import cats.effect._
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{BasicCredentials, HttpRoutes}
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig.generic.auto._
import votelog.app
import votelog.domain.authentication.User
import votelog.domain.authorization.Component.Root
import votelog.implementation.Log4SLogger
import votelog.service._

object Webserver extends IOApp {

  implicit val log = new Log4SLogger[IO](org.log4s.getLogger)

  def run(args: List[String]): IO[ExitCode] =
    for {
      configuration <- loadConfiguration
      _ <- log.info(s"configuration: $configuration")
      voteLog = VoteLog[IO](configuration.votelog)
      runServer <- voteLog.use { voteLog =>
        val routes = setupHttpRoutes(configuration.votelog.security, voteLog)

        runVotelogWebserver(configuration.http, routes)
      }
    } yield runServer

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
      configuration: app.Configuration.Security,
      votelog: VoteLog[IO]
  ): HttpRoutes[IO] = {

    object component {
      val root = Root.child("api").child("v0")
      val person = root.child("person")
      val motion = root.child("motion")
      val user = root.child("user")
      val ngo = root.child("ngo")
      val auth = root.child("auth")
    }

    val pws =
      new PersonService(
        component = component.person,
        store = votelog.politician,
        voteAlg = votelog.vote,
        log = log,
        authAlg = votelog.authorization
      )

    val mws = new MotionService(component.motion, votelog.motion, votelog.authorization)
    val uws = new UserService(component.user, votelog.user, votelog.authorization)
    val nws = new NgoService(component.ngo, votelog.ngo, votelog.authorization)

    val key = PrivateKey(configuration.secret.getBytes)
    val crypto: CryptoBits = CryptoBits(key)
    val clock = Clock[IO]
    val auth: AuthMiddleware[IO, User] = new AuthenticationService(votelog.user, crypto).middleware

    val validateCredentials: BasicCredentials => IO[Option[User]] = { creds =>
      for {
        maybeUser <- votelog.user.findByName(creds.username)
        hashedPassword <- votelog.passwordHasher.hashPassword(creds.password)
      } yield maybeUser.filter(_.passwordHash === hashedPassword)
    }

    val basicAuth: AuthMiddleware[IO, User] = BasicAuth("votelog", validateCredentials)
    val session = new SessionService(crypto, clock, component.root)

    val service =
      Router(
        component.person.location -> auth(pws.service),
        component.motion.location -> auth(mws.service),
        component.user.location -> auth(uws.service),
        component.ngo.location -> auth(nws.service),
        component.auth.location -> CORS(basicAuth(session.service)),
      )

    service
  }

  lazy val loadConfiguration =
    for {
      votelog <- IO(pureconfig.loadConfigOrThrow[app.Configuration]("votelog"))
      http <- IO(pureconfig.loadConfigOrThrow[Configuration.Http]("webapp.http"))
    } yield Configuration(votelog, http)

  case class Configuration(votelog: app.Configuration, http: Configuration.Http)

  object Configuration {
    case class Http(port: Int, interface: String)
  }
}
