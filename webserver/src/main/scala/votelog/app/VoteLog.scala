package votelog.app

import cats._
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import votelog.app.Database.buildTransactor
import votelog.app.Webserver.log
import votelog.crypto.PasswordHasherJavaxCrypto.Salt
import votelog.crypto.{PasswordHasherAlg, PasswordHasherJavaxCrypto}
import votelog.domain.authorization.AuthorizationAlg
import votelog.domain.politics.Scoring.{Score, Weight}
import votelog.domain.politics.{Motion, Ngo, Votum}
import votelog.implementation.UserCapabilityAuthorization
import votelog.infrastructure.VoteAlg
import votelog.infrastructure.logging.Logger
import votelog.persistence.doobie._
import votelog.persistence._

abstract class VoteLog[F[_]: Applicative] {
  val vote: VoteAlg[F]
  val politician: PersonStore[F]
  val motion: MotionStore[F]
  val user: UserStore[F]
  val ngo: NgoStore[F]
  val authorization: AuthorizationAlg[F]
  val passwordHasher: PasswordHasherAlg[F]

  // TODO: this should not be part of the store. extract into separate
  // service/alg and use stores from there
  def politiciansScoredBy(ngos: Map[Ngo.Id, Weight]): F[List[(Motion.Id, Score)]] = {

    ngos
      .toList
      .traverse { case (id, weight) =>
        ngo.motionsScoredBy(id)
      }


    ???
  }

}

object VoteLog {

  def apply[F[_]: Async: ContextShift: Logger](configuration: Configuration): Resource[F, VoteLog[F]] = {
    val hasher = new PasswordHasherJavaxCrypto[F](Salt(configuration.security.passwordSalt))
    val db = buildTransactor(configuration.database)
    val cv = buildTransactor(configuration.curiaVista)

    Resource.pure(buildAppAlg(hasher, db, cv))
  }

  def buildAppAlg[F[_]: Sync: ThrowableBracket](
    hasher: PasswordHasherAlg[F],
    votelogDatabase: Transactor[F],
    curiaVistaDatabase: Transactor[F]
  ): VoteLog[F] =
    new VoteLog[F] {
      val politician = new DoobiePersonStore(curiaVistaDatabase)
      val vote = new DoobieVoteStore(curiaVistaDatabase)
      val motion = new DoobieMotionStore(curiaVistaDatabase)
      val user = new DoobieUserStore(votelogDatabase, hasher)
      val ngo = new DoobieNgoStore(votelogDatabase)
      val authorization = new UserCapabilityAuthorization
      val passwordHasher = hasher
    }
}
