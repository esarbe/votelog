package votelog.app

import cats.effect._
import doobie._
import doobie.h2.H2Transactor
import doobie.implicits._
import votelog.app.Webserver.log
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.crypto.{PasswordHasherAlg, PasswordHasherJavaxCrypto}
import votelog.domain.authorization.AuthorizationAlg
import votelog.implementation.UserCapabilityAuthorization
import votelog.infrastructure.VoteAlg
import votelog.persistence.doobie._
import votelog.persistence.{MotionStore, PoliticianStore, UserStore}

trait VoteLog[F[_]] {
  val vote: VoteAlg[F]
  val politician: PoliticianStore[F]
  val motion: MotionStore[F]
  val user: UserStore[F]
  val authorization: AuthorizationAlg[F]
  val passwordHasher: PasswordHasherAlg[F]
}

object VoteLog {

  def apply[F[_]: Async: ContextShift](configuration: Configuration): Resource[F, VoteLog[F]] = {
    val hasher = new PasswordHasherJavaxCrypto[F](Salt(configuration.security.passwordSalt))

    setupDatabase[F](configuration.database).map {
      transactor =>
        new VoteLog[F] {
          val politician = new DoobiePoliticianStore(transactor)
          val vote = new DoobieVoteStore(transactor)
          val motion = new DoobieMotionStore(transactor)
          val user = new DoobieUserStore(transactor, hasher)
          val ngo = new DoobieNgoStore(transactor)
          val authorization = new UserCapabilityAuthorization
          val passwordHasher = hasher
        }
    }
  }


  def setupDatabase[F[_]: Async: ContextShift](
    config: Configuration.Database
  ): Resource[F, Transactor[F]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32) // connection EC
      te <- ExecutionContexts.cachedThreadPool[F]    // transaction EC
      xa <- H2Transactor.newH2Transactor[F](
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce, // await connection here
        transactEC = te, // execute JDBC operations here
      )
    } yield xa: Transactor[F]
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
}