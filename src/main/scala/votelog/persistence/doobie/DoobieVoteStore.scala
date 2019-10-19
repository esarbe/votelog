package votelog.persistence.doobie

import cats.Monad
import votelog.domain.politics.{Motion, Person}
import votelog.infrastructure.VoteAlg
import doobie.implicits._
import cats.implicits._
import votelog.domain.politics.Votum
import doobie.postgres.implicits._

class DoobieVoteStore[F[_]: Monad: ThrowableBracket](
  transactor:  doobie.util.transactor.Transactor[F]
) extends VoteAlg[F] {

  import Mappings._

  override def getVotesForMotion(m: Motion.Id): F[List[(Person.Id, Votum)]] =
    sql"select motionid, votum from votes where businessid = ${m.value}".query[(Person.Id, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)

  override def getVotesForPerson(p: Person.Id): F[List[(Motion.Id, Votum)]] =
    sql"select motionid, votum from votes where personid = ${p.value}".query[(Motion.Id, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)
}
