package votelog.persistence.doobie

import cats.Monad
import cats.data.NonEmptyList
import votelog.domain.model.{Motion, Politician, Votum}
import votelog.infrastructure.VoteAlg
import doobie.implicits._
import cats.implicits._

abstract class DoobieVoteStore[F[_]: Monad] extends VoteAlg[F] {

  import Mappings._

  val transactor: doobie.util.transactor.Transactor[F]

  def insertQuery(pid: Politician.Id, mid: Motion.Id, v: Votum) =
    sql"insert into vote (politicianid, motionid, votum) values ($pid, $mid, 'yes')"

  override def voteFor(p: Politician.Id, m: Motion.Id, v: Votum): F[Unit] =
    insertQuery(p, m, v)
      .update
      .run
      .transact(transactor)
      .map(_ => Unit)

  override def getVotes(p: Politician.Id): F[List[(Motion, Votum)]] =
    sql"select * from ".query[(Motion, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)
}
