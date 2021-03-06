package votelog.app

import cats._
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import votelog.app.Database.buildTransactor
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.crypto.{PasswordHasherAlg, PasswordHasherJavaxCrypto}
import votelog.domain.authorization.AuthorizationAlg
import votelog.domain.politics.Scoring.{Score, Weight}
import votelog.domain.politics.{Business, Ngo, VoteAlg}
import votelog.implementation.UserCapabilityAuthorization
import votelog.persistence._
import votelog.persistence.doobie._

abstract class VoteLog[F[_]] {
  val vote: VoteAlg[F]
  val politician: PersonStore[F]
  val motion: BusinessStore[F]
  val user: UserStore[F]
  val ngo: NgoStore[F]
  val authorization: AuthorizationAlg[F]
  val passwordHasher: PasswordHasherAlg[F]
}

object VoteLog {

  def apply[F[_]: ContextShift: NonEmptyParallel: Async](configuration: Configuration): Resource[F, VoteLog[F]] = {
    val hasher = new PasswordHasherJavaxCrypto[F](Salt(configuration.security.passwordSalt))
    val db = buildTransactor(configuration.database)
    val cv = buildTransactor(configuration.curiaVista)

    Resource.pure[F, VoteLog[F]](buildAppAlg(hasher, db, cv))
  }

  def buildAppAlg[F[_]: NonEmptyParallel: Sync](
    hasher: PasswordHasherAlg[F],
    votelogDatabase: Transactor[F],
    curiaVistaDatabase: Transactor[F]
  ): VoteLog[F] =
    new VoteLog[F] {
      val politician = new DoobiePersonStore(curiaVistaDatabase)
      val vote = new DoobieVoteStore(curiaVistaDatabase)
      val motion = new DoobieBusinessStore(curiaVistaDatabase)
      val user = new DoobieUserStore(votelogDatabase, hasher)
      val ngo = new DoobieNgoStore(votelogDatabase)
      val authorization = new UserCapabilityAuthorization
      val passwordHasher = hasher
    }
}
