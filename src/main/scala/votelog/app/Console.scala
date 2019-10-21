package votelog
package app


import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.implicits._
import doobie.util.transactor.Transactor
import votelog.implementation.Log4SLogger
import votelog.persistence.{PersonStore, UserStore}
import votelog.domain.authorization.{Capability, Component, User}
import votelog.persistence.UserStore.Password
import votelog.persistence.doobie.DoobieSchema
import pureconfig.generic.auto._
import votelog.infrastructure.StoreAlg
object Console extends IOApp {

  implicit val log = new Log4SLogger[IO](org.log4s.getLogger)

  case class Configuration(votelog: app.Configuration)

  val loadConfiguration: IO[Configuration] =  IO(pureconfig.loadConfigOrThrow[Configuration]("console"))

  def run(args: List[String]): IO[ExitCode] =
    for {
      configuration <- loadConfiguration
      votelogTransactor = Database.buildTransactor[IO](configuration.votelog.database)
      _ <- log.info(s"configuration: $configuration")
      voteLog = VoteLog[IO](configuration.votelog)
      _ <- new DoobieSchema[IO](votelogTransactor).initialize
      _ <- voteLog.use(v => setupAdmin(v.user))
    } yield ExitCode.Success


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

}
