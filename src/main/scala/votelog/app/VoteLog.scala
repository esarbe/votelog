package votelog.app

import cats._
import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import doobie._
import doobie.h2.H2Transactor
import doobie.implicits._
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.crypto.{PasswordHasherAlg, PasswordHasherJavaxCrypto}
import votelog.domain.authorization.AuthorizationAlg
import votelog.implementation.UserCapabilityAuthorization
import votelog.infrastructure.VoteAlg
import votelog.infrastructure.logging.Logger
import votelog.persistence.doobie._
import votelog.persistence.{MotionStore, NgoStore, PoliticianStore, Schema, UserStore}

trait VoteLog[F[_]] {
  val vote: VoteAlg[F]
  val politician: PoliticianStore[F]
  val motion: MotionStore[F]
  val user: UserStore[F]
  val ngo: NgoStore[F]
  val authorization: AuthorizationAlg[F]
  val passwordHasher: PasswordHasherAlg[F]
}

object VoteLog {

  def apply[F[_]: Async: ContextShift: Logger](configuration: Configuration): Resource[F, VoteLog[F]] = {
    val hasher = new PasswordHasherJavaxCrypto[F](Salt(configuration.security.passwordSalt))

    connectToDatabase[F](configuration.database)
      .evalMap { transactor =>
        initializeDatabase(new DoobieSchema(transactor)) *>
          Async[F].delay(transactor)
      }
      .map(buildAppAlg(hasher))
  }

  def buildAppAlg[F[_]: Monad](
    hasher: PasswordHasherAlg[F])(
    transactor: Transactor[F]
  ): VoteLog[F] =
    new VoteLog[F] {
      val politician = new DoobiePoliticianStore(transactor)
      val vote = new DoobieVoteStore(transactor)
      val motion = new DoobieMotionStore(transactor)
      val user = new DoobieUserStore(transactor, hasher)
      val ngo = new DoobieNgoStore(transactor)
      val authorization = new UserCapabilityAuthorization
      val passwordHasher = hasher
    }


  def connectToDatabase[F[_]: Async: ContextShift](
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


  private def initializeDatabase[F[_]: ContextShift: Async: Logger](pt: Schema[F]): F[Unit] =
    for {
      _ <- Logger[F].info("Deleting and re-creating database")
      _ <- pt.initialize
      _ <- Logger[F].info("Deleting and re-creating database successful")
    } yield ()
}