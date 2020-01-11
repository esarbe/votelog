package votelog.infrastructure

import votelog.domain.politics.Votum
import votelog.domain.politics.{Business, Person}

trait VoteAlg[F[_]] {
  def getVotesForMotion(m: Business.Id): F[List[(Person.Id, Votum)]]
  def getVotesForPerson(p: Person.Id): F[List[(Business.Id, Votum)]]
}
