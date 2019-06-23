package votelog.app

import cats.effect.{ExitCode, IO, IOApp}
import votelog.implementation.Log4SLogger
import votelog.persistence.{PoliticianStore, UserStore}
import votelog.app.Webserver.log
import votelog.domain.authorization.{Capability, Component, User}
import votelog.persistence.UserStore.Password
import votelog.persistence.doobie.DoobieSchema
import pureconfig.generic.auto._
object Console extends IOApp {
  implicit val log = new Log4SLogger[IO](org.log4s.getLogger)
  val loadConfiguration: IO[Configuration] =  IO(pureconfig.loadConfigOrThrow[Configuration]("votelog.webapp"))

  def run(args: List[String]): IO[ExitCode] =
    for {
      configuration <- loadConfiguration
      transactor = Database.buildTransactor[IO](configuration.database)
      voteLog = VoteLog[IO](configuration)
      runServer <- voteLog.use(setup)
    } yield runServer


  private def setupAdmin(user: UserStore[IO]) =
    for {
      _ <- log.info("setting up initial admin account")
      id <- user.create(
        UserStore.Recipe("admin", User.Email("admin@votelog.ch"), Password.Clear("foo")))
      _ <- user.grantPermission(id, Component.Root, Capability.Create)
      _ <- user.grantPermission(id, Component.Root, Capability.Read)
      _ <- user.grantPermission(id, Component.Root, Capability.Update)
      _ <- user.grantPermission(id, Component.Root, Capability.Delete)
      _ <- log.info("admin account created")
      user <- user.findByName("admin")
      _ <- log.info(s"user: $user")
    } yield ()

  def setup(votelog: VoteLog[IO]) =
    for {
      _ <- log.info("creating politician foo")
      id <- votelog.politician.create(PoliticianStore.Recipe("foo"))
      _ <- log.info(s"id got back id: $id")
      ids <- votelog.politician.index
      _ <- log.info(s"ids: ${ids.mkString(",")}")
      p <- votelog.politician.read(id)
      _ <- log.info(s"got back politician: $p")
    } yield ExitCode.Success
}
