package votelog.persistence.doobie

import cats.Monad
import votelog.domain.politics.{Business, Person, VoteAlg, Votum}
import doobie._
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._

class DoobieVoteStore[F[_]: Monad: ThrowableBracket](
  transactor:  doobie.util.transactor.Transactor[F]
) extends VoteAlg[F] {

  import votelog.orphans.doobie.implicits._

  override def getVotesForBusiness(m: Business.Id): F[List[(Person.Id, Votum)]] =
    sql"select person_number, decision from voting where business_number = ${m.value}".query[(Person.Id, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)

  override def getVotesForPerson(p: Person.Id): F[List[(Business.Id, Votum)]] =
    sql"select business_number, decision from voting where person_number = ${p} = ${p.value}"
      .query[(Business.Id, Votum)]
      .stream
      .compile
      .toList
      .transact(transactor)
}
