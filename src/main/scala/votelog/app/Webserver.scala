package votelog.app

import cats.Monad
import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.h2._
import doobie.implicits._
import org.http4s.{BasicCredentials, HttpRoutes}
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.authentication.BasicAuth
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig.ConfigReader.Result
import votelog.domain.politics.{Motion, Politician, Votum}
import votelog.implementation.Log4SLogger
import votelog.infrastructure.{StoreAlg, VoteAlg}
import votelog.persistence.{MotionStore, PoliticianStore, UserStore}
import votelog.persistence.doobie.{DoobieMotionStore, DoobiePoliticianStore, DoobieSchema, DoobieUserStore, DoobieVoteStore}
import votelog.service.{AuthenticationService, MotionService, PoliticianService, SessionService}
import pureconfig.generic.auto._
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import votelog.crypto.PasswordHasherJavaxCrypto
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.domain.authorization.Component.Root
import votelog.domain.authorization.{AuthAlg, Capability, Component, User}

object Webserver extends IOApp {

  val log = new Log4SLogger[IO](org.log4s.getLogger)
  val loadConfiguration: IO[Configuration] = loadConfigF[IO, Configuration]("votelog.webapp")

  val transactor: Resource[IO, H2Transactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]    // our transaction EC
      xa <- H2Transactor.newH2Transactor[IO](
        "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", // connect URL
        "sa",                                   // username
        "",                                     // password
        ce,                                     // await connection here
        te                                      // execute JDBC operations here
      )
    } yield xa

  def run(args: List[String]): IO[ExitCode] =
    transactor.use { xa: Transactor[IO] =>

      val schema: DoobieSchema = new DoobieSchema

      val votelog: VoteLog[IO] = new VoteLog[IO] {
        val politician = new DoobiePoliticianStore[IO](xa)
        val vote = new DoobieVoteStore[IO](xa)
        val motion = new DoobieMotionStore[IO](xa)
      }
      //val userStore = new DoobieUserStore[IO](xa)

      val auth = new AuthAlg[IO] {
        override def hasCapability[C](user: User, capability: Capability, component: Component): IO[Boolean] = {
          IO(user.permissions.filter(_.component.contains(component)).map(_.capability).contains(capability))
        }
      }

      val pws = new PoliticianService(Root.child("politician"), votelog.politician, votelog.vote, log, auth)
      val mws = new MotionService(Root.child("motion"), votelog.motion, auth)

      setupEnvironment(xa, schema) *>
        createTestData(votelog)
          .attempt
          .flatMap {
            case Left(error) =>
              log.error(error)(s"something went wrong while initialising data: ${error.getMessage}")
            case Right(_) => log.info("ol korrekt")
          } *>
        loadConfiguration.flatMap(config =>
          startVotlogWebserver(config, pws, mws, xa))
    }


  private def startVotlogWebserver(
    configuration: Configuration,
    pws: PoliticianService,
    mws: MotionService,
    transactor: Transactor[IO],
  ): IO[ExitCode] = {

    val key = PrivateKey(configuration.security.secret.getBytes)
    val crypto: CryptoBits = CryptoBits(key)
    val passwordHasherAlg = new PasswordHasherJavaxCrypto[IO](Salt(configuration.security.salt))
    val clock = Clock[IO]

    val userStore: UserStore[IO] = new DoobieUserStore(transactor)
    val auth = new AuthenticationService(userStore, crypto).middleware

    val validateCredentials: BasicCredentials => IO[Option[User]] = {
      creds =>
        for {
          maybeUser <- userStore.findByName(creds.username)
          hashedPassword <- passwordHasherAlg.hashPassword(creds.password)
        } yield maybeUser.filter(_.hashedPassword == hashedPassword)
    }

    val basicAuth: AuthMiddleware[IO, User] = BasicAuth("votelog", validateCredentials)

    val httpRoutes: HttpRoutes[IO] =
      Router(
        "/api/v0/politician" -> auth(pws.service),
        "/api/v0/motion" -> auth(mws.service),
        "/api/v0/auth" -> basicAuth(new SessionService(crypto, clock).service)
      )

    val server = for {
      _ <- log.info(s"attempting to bind to port ${configuration.http.port}")
      server <-
      BlazeServerBuilder[IO]
        .bindHttp(configuration.http.port, configuration.http.interface)
        .withHttpApp(httpRoutes.orNotFound)
        .serve
        .compile
        .drain

    } yield server

    server.as(ExitCode.Success)
  }

  private def setupEnvironment(
    xa: Transactor[IO],
    pt: DoobieSchema,
  ): IO[ExitCode] = {

    for {
      _ <- log.info("Deleting and re-creating database")
      _ <- pt.initialize.transact(xa)
      _ <- log.info("Deleting and re-creating database successful")

    } yield ExitCode.Success
  }

  private def createTestData(
    votelog: VoteLog[IO],
  ): IO[ExitCode] = {

    for {
      fooId <- votelog.politician.create(PoliticianStore.Recipe("foo"))
      barId <- votelog.politician.create(PoliticianStore.Recipe("bar"))
      _ <- log.info(s"foo has id '$fooId'")
      _ <- log.info(s"bar has id '$barId'")
      _ <- votelog.politician.read(fooId)
      ids <- votelog.politician.index
      _ <- ids.map(id => log.info(id.toString)).sequence
      _ <-
      ids
        .headOption
        .map(votelog.politician.read)
        .map(_.flatMap(p => log.info(s"found politician '$p'")))
        .getOrElse(log.warn("unable to find any politician"))
      //_ <- ps.delete(Politician.Id(4))

      _ <- votelog.motion.create(MotionStore.Recipe("eat the rich 2", Politician.Id(1)))

      // motions
      motions <- votelog.motion.index.flatMap(_.map(votelog.motion.read).sequence)
      _ <- motions.map(m => log.info(s"found motion: $m")).sequence
      _ <- votelog.vote.voteFor(Politician.Id(1), Motion.Id(1), Votum.Yes)
    } yield ExitCode.Success
  }


  trait VoteLog[F[_]] {
    val vote: VoteAlg[F]
    val politician: PoliticianStore[F]
    val motion: MotionStore[F]
  }


  case class Configuration(
    http: Configuration.Http,
    security: Configuration.Security
  )

  object Configuration {
    case class Http(port: Int, interface: String)
    case class Security(salt: String, secret: String)
  }
}
