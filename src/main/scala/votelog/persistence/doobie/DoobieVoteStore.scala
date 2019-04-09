package votelog.persistence.doobie

import cats.Monad
import votelog.domain.model.{Motion, Politician, Votum}
import votelog.infrastructure.VoteAlg
import doobie.implicits._
import Mappings._

abstract class DoobieVoteStore[F[_]: Monad] extends VoteAlg[F]{

  val transactor: doobie.util.transactor.Transactor[F]

  def insertQuery(pid: Politician.Id, mid: Motion.Id, v: Votum) =
    sql"insert into vote (politicianid, motionid, votum) values ($pid, $mid, $v)"

  override def voteFor(p: Politician.Id, m: Motion.Id, v: Votum): F[Unit] =
    insertQuery(p, m, v)
      .update
      .withUniqueGeneratedKeys("policiticianid", "motionid")
      .transact(transactor)
}
