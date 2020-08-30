package votelog.app

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRequest, AuthedRoutes, BasicCredentials, HttpRoutes, Request, Response, Status}
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import votelog.app
import votelog.domain.authentication.User
import votelog.domain.authorization.Component.Root
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, Person}
import votelog.endpoint.PersonStoreEndpoint
import votelog.implementation.Log4SLogger
import votelog.service._

import scala.concurrent.ExecutionContext

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
      BlazeServerBuilder[IO](ExecutionContext.global)
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
      val api = Root.child("api").child("v0")
      val person = api.child("person")
      val business = api.child("business")
      val user = api.child("user")
      val ngo = api.child("ngo")
      val auth = api.child("auth")
    }

    val pws =
      new PersonService(
        component = component.person,
        store = votelog.person,
        voteAlg = votelog.vote,
        log = log,
        authAlg = votelog.authorization
      )

    val bws = new BusinessService(component.business, votelog.motion, votelog.authorization, votelog.vote)
    val uws = new UserService(component.user, votelog.user, votelog.authorization)
    val nws = new NgoService(component.ngo, votelog.ngo, votelog.authorization)

    val key = PrivateKey(configuration.secret.getBytes)
    val crypto: CryptoBits = CryptoBits(key)
    val clock = Clock[IO]
    val authService = new AuthenticationService(votelog.user, crypto)
    val auth: AuthMiddleware[IO, User] = authService.middleware

    val validateCredentials: BasicCredentials => IO[Option[User]] = { creds =>
      for {
        maybeUser <- votelog.user.findByName(creds.username)
        hashedPassword <- votelog.passwordHasher.hashPassword(creds.password)
      } yield maybeUser.filter(_.passwordHash === hashedPassword)
    }

    val basicAuth: AuthMiddleware[IO, User] = BasicAuth("votelog", validateCredentials)
    val session = new SessionService(crypto, clock, component.api)

    val personStoreEndpoint = new PersonStoreHttp4sEndpoint(votelog.person)

    val services =
      Map(
        //"qux" -> personStoreEndpoint.routes,
        component.person.location -> auth(pws.service),
        component.business.location -> auth(bws.service),
        component.user.location -> auth(uws.service),
        component.ngo.location -> auth(nws.service),
        component.auth.child("session").location -> basicAuth(session.service),
        component.auth.child("user").location -> auth(session.service),
      )

    Router(services.view.mapValues(CORS(_)).toSeq:_*) <+>
      HttpRoutes.of(personStoreEndpoint.routes)
  }

  lazy val loadConfiguration =
    for {
      votelog <- IO(ConfigSource.default.at("votelog").loadOrThrow[app.Configuration])
      http <- IO(ConfigSource.default.at("webapp.http").loadOrThrow[Configuration.Http])
    } yield Configuration(votelog, http)

  case class Configuration(votelog: app.Configuration, http: Configuration.Http)

  object Configuration {
    case class Http(port: Int, interface: String)
  }
}
