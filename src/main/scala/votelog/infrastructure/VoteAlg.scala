package votelog.infrastructure

import votelog.domain.model.{Motion, Politician, Votum}

trait VoteAlg[F[_]] {
  def voteFor(p: Politician.Id, m: Motion.Id, v: Votum): F[Unit]
  def getVotes(p: Politician.Id): F[List[(Motion.Id, Votum)]]
}
