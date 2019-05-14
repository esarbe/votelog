package votelog.app

import cats.effect._
import cats.implicits._
import doobie._
import doobie.h2._
import doobie.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{BasicCredentials, HttpRoutes}
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import votelog.crypto.{PasswordHasherAlg, PasswordHasherJavaxCrypto}
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.domain.authorization.Component.Root
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component, User}
import votelog.domain.politics.{Motion, Politician, Votum}
import votelog.implementation.Log4SLogger
import votelog.infrastructure.VoteAlg
import votelog.persistence.doobie._
import votelog.persistence.{MotionStore, PoliticianStore, UserStore}
import votelog.service._

object Webserver extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      configuration <- loadConfiguration
      transactor = setupDatabase(configuration.database)
      _ <- transactor.use(initializeDatabase(new DoobieSchema))
      runServer <- transactor.use { xa =>

        val passwordHasher = new PasswordHasherJavaxCrypto[IO](Salt(configuration.security.passwordSalt))
        val votelog = buildEnvironment(xa, passwordHasher)
        val routes = setupHttpRoutes(configuration.security, passwordHasher, votelog)

        setupAdmin(votelog.user).flatMap( _ =>
          runVotelogWebserver(configuration.http, routes)
        )
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
  ): IO[ExitCode] = {

    val server = for {
      _ <- log.info(s"attempting to bind to port ${config.port}")
      server <-
      BlazeServerBuilder[IO]
        .bindHttp(config.port, config.interface)
        .withHttpApp(routes.orNotFound)
        .serve
        .compile
        .drain

    } yield server

    server.as(ExitCode.Success)
  }


  private def initializeDatabase(
    pt: DoobieSchema)(
    xa: Transactor[IO]
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


  def buildEnvironment(
    transactor: Transactor[IO],
    passwordHasher: PasswordHasherAlg[IO]
  ): VoteLog[IO] = {

    new VoteLog[IO] {
      val politician = new DoobiePoliticianStore(transactor)
      val vote = new DoobieVoteStore(transactor)
      val motion = new DoobieMotionStore(transactor)
      val user = new DoobieUserStore(transactor, passwordHasher)
      val ngo = new DoobieNgoStore(transactor)
    }
  }


  def setupHttpRoutes(
    configuration: Configuration.Security,
    passwordHasher: PasswordHasherAlg[IO],
    votelog: VoteLog[IO]
  ): HttpRoutes[IO] = {

    val authorization = new AuthorizationAlg[IO] {
      override def hasCapability[C](user: User, capability: Capability, component: Component): IO[Boolean] = {
        IO(user.permissions.filter(_.component.contains(component)).map(_.capability).contains(capability))
      }
    }

    val pws = new PoliticianService(Root.child("politician"), votelog.politician, votelog.vote, log, authorization)
    val mws = new MotionService(Root.child("motion"), votelog.motion, authorization)
    val uws = new UserService(Root.child("user"), votelog.user, authorization)

    val key = PrivateKey(configuration.secret.getBytes)
    val crypto: CryptoBits = CryptoBits(key)

    val clock = Clock[IO]

    val auth = new AuthenticationService(votelog.user, crypto).middleware

    val validateCredentials: BasicCredentials => IO[Option[User]] = {
      creds =>
        for {
          maybeUser <- votelog.user.findByName(creds.username)
          hashedPassword <- passwordHasher.hashPassword(creds.password)
        } yield maybeUser.filter(_.hashedPassword == hashedPassword)
    }

    val basicAuth: AuthMiddleware[IO, User] = BasicAuth("votelog", validateCredentials)
    val session = new SessionService(crypto, clock)

    Router(
      "/api/v0/politician" -> auth(pws.service),
      "/api/v0/motion" -> auth(mws.service),
      "/api/v0/auth" -> basicAuth(session.service),
      "/api/v0/user" -> auth(uws.service),
    )

  }

  def setupDatabase(config: Configuration.Database): Resource[IO, Transactor[IO]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]    // our transaction EC
      xa <- H2Transactor.newH2Transactor[IO](
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce, // await connection here
        transactEC = te, // execute JDBC operations here
      )
    } yield xa: Transactor[IO]
  }


  val log = new Log4SLogger[IO](org.log4s.getLogger)

  val loadConfiguration: IO[Configuration] = loadConfigF[IO, Configuration]("votelog.webapp")

  trait VoteLog[F[_]] {
    val vote: VoteAlg[F]
    val politician: PoliticianStore[F]
    val motion: MotionStore[F]
    val user: UserStore[F]
  }


  case class Configuration(
    http: Configuration.Http,
    security: Configuration.Security,
    database: Configuration.Database
  )

  object Configuration {
    case class Http(port: Int, interface: String)
    case class Security(passwordSalt: String, secret: String)
    case class Database(url: String, user: String, password: String)
  }
}
