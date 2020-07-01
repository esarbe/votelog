package votelog.domain.politics

trait VoteAlg[F[_]] {
  // we probably need some kind of paging as well
  def getVotesForBusiness(context: Context)(business: Business.Id): F[List[(Person.Id, Votum)]]
  def getVotesForPerson(context: Context)(person: Person.Id): F[List[(Business.Id, Votum)]]
}
