package votelog.infrastructure

import votelog.domain.politics.Votum
import votelog.domain.politics.{Motion, Politician}

trait VoteAlg[F[_]] {
  def voteFor(p: Politician.Id, m: Motion.Id, v: Votum): F[Unit]
  def getVotes(p: Politician.Id): F[List[(Motion.Id, Votum)]]
}
