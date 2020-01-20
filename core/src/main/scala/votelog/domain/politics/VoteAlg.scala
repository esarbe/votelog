package votelog.domain.politics

trait VoteAlg[F[_]] {
  def getVotesForBusiness(m: Business.Id): F[List[(Person.Id, Votum)]]
  def getVotesForPerson(p: Person.Id): F[List[(Business.Id, Votum)]]
}
