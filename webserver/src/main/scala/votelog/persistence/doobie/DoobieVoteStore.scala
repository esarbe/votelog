package votelog.persistence.doobie

import cats.Monad
import votelog.domain.politics.{Business, Language, LegislativePeriod, Person, VoteAlg, Votum}
import doobie._
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._

class DoobieVoteStore[F[_]: Monad: ThrowableBracket](
  transactor:  doobie.util.transactor.Transactor[F]
) extends VoteAlg[F] {

  import votelog.orphans.doobie.implicits._

  override def getVotesForBusiness(
    legislativePeriod: LegislativePeriod.Id,
    language: Language,
    businessId: Business.Id
  ): F[List[(Person.Id, Votum)]] = {
    sql"""
      |select person_number, decision from voting
      |where anguage = $language
      |and id_legislative_period = ${legislativePeriod}
      |and business_number = ${businessId}
      |"""
        .stripMargin
        .query[(Person.Id, Votum)]
        .stream
        .compile
        .toList
        .transact(transactor)
  }

  override def getVotesForPerson(
    legislativePeriod: LegislativePeriod.Id,
    language: Language,
    person: Person.Id
  ): F[List[(Business.Id, Votum)]] = {
    sql"""
      |select business_number, decision from voting v
      |where id_legislative_period = ${legislativePeriod}
      |and `language` = $language
      |and subject  = "Vote final"
      |and p.person_number = 3893
      |"""
        .stripMargin
        .query[(Business.Id, Votum)]
        .stream
        .compile
        .toList
        .transact(transactor)
  }
}
