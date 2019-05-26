package votelog.persistence.doobie

import cats.Monad
import votelog.domain.politics.{Motion, Politician}
import votelog.infrastructure.VoteAlg
import doobie.implicits._
import cats.implicits._
import votelog.domain.politics.Votum

class DoobieVoteStore[F[_]: Monad](
  transactor:  doobie.util.transactor.Transactor[F]
) extends VoteAlg[F] {

  import Mappings._

  def insertQuery(pid: Politician.Id, mid: Motion.Id, v: Votum) =
    sql"insert into vote (politicianid, motionid, votum) values ($pid, $mid, $v)"

  override def voteFor(p: Politician.Id, m: Motion.Id, v: Votum): F[Unit] =
    insertQuery(p, m, v)
      .update
      .run
      .transact(transactor)
      .map(_ => Unit)

  override def getVotes(p: Politician.Id): F[List[(Motion.Id, Votum)]] =
    sql"select motionid, votum from vote where politicianid = ${p.value}".query[(Motion.Id, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)
}
