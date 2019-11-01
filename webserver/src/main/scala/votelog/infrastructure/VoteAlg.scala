package votelog.infrastructure

import votelog.domain.politics.Votum
import votelog.domain.politics.{Motion, Person}

trait VoteAlg[F[_]] {
  def getVotesForMotion(m: Motion.Id): F[List[(Person.Id, Votum)]]
  def getVotesForPerson(p: Person.Id): F[List[(Motion.Id, Votum)]]
}
