package votelog.domain.politics

trait VoteAlg[F[_]] {
  def getVotesForBusiness(
    legislativePeriod: LegislativePeriod.Id,
    language: Language,
    business: Business.Id
  ): F[List[(Person.Id, Votum)]]

  def getVotesForPerson(
    legislativePeriod: LegislativePeriod.Id,
    language: Language,
    person: Person.Id
  ): F[List[(Business.Id, Votum)]]
}
