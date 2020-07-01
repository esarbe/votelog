package votelog.persistence.doobie

import cats.Monad
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, VoteAlg, Votum}
import doobie._
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._

class DoobieVoteStore[F[_]: Monad: ThrowableBracket](
  transactor:  doobie.util.transactor.Transactor[F]
) extends VoteAlg[F] {

  import votelog.orphans.doobie.implicits._

  override def getVotesForBusiness(context: Context)(businessId: Business.Id): F[List[(Person.Id, Votum)]] = {
    sql"""
      |select person_number, decision from voting
      |where language = ${context.language}
      |and id_legislative_period = ${context.legislativePeriod}
      |and subject = "Vote final"
      |and business_number = ${businessId}
      |"""
        .stripMargin
        .query[(Person.Id, Votum)]
        .stream
        .compile
        .toList
        .transact(transactor)
  }

  override def getVotesForPerson(context: Context)(person: Person.Id): F[List[(Business.Id, Votum)]] = {
    sql"""
      |select business_number, decision from voting
      |where id_legislative_period = ${context.legislativePeriod}
      |and `language` = ${context.language}
      |and subject  = "Vote final"
      |and person_number = ${person}
      |"""
        .stripMargin
        .query[(Business.Id, Votum)]
        .stream
        .compile
        .toList
        .transact(transactor)
  }
}
