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
import votelog.domain.politics.Votum
import votelog.implementation.UserCapabilityAuthorization
import votelog.infrastructure.VoteAlg
import votelog.infrastructure.logging.Logger
import votelog.persistence.doobie._
import votelog.persistence._

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

    Resource.pure[F, Transactor[F]](buildTransactor[F](configuration.database))
      .evalMap(Async[F].delay(_))
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

  private def createTestData[F[_]: ContextShift: Async: Logger](
    services: VoteLog[F],
  ) =
    for {
      fooId <- services.politician.create(PoliticianStore.Recipe("foo"))
      barId <- services.politician.create(PoliticianStore.Recipe("bar"))
      _ <- Logger[F].info(s"foo has id '$fooId'")
      _ <- Logger[F].info(s"bar has id '$barId'")
      _ <- services.politician.read(fooId)
      ids <- services.politician.index
      _ <- ids.map(id => Logger[F].info(id.toString)).sequence
      _ <- ids.headOption
        .map(services.politician.read)
        .map(_.flatMap(p => Logger[F].info(s"found politician '$p'")))
        .getOrElse(Logger[F].warn("unable to find any politician"))
      //_ <- ps.delete(Politician.Id(4))

      _ <- services.motion.create(MotionStore.Recipe("eat the rich 2", fooId))

      // motions
      motionIds <- services.motion.index
      motions <- motionIds.map(services.motion.read).sequence
      _ <- motions.map(m => Logger[F].info(s"found motion: $m")).sequence
      _ <- services.vote.voteFor(fooId, motionIds.head, Votum.Yes)
    } yield services
}
