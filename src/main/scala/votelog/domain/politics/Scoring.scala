package votelog.domain.politics

import votelog.domain.politics.Scoring.Score

trait Scoring[F[_]] {
  def motionsScoredBy(ngo: Ngo.Id): F[List[(Motion.Id, Score)]]
  def politiciansScoredBy(ngo: Ngo.Id): F[List[(Motion.Id, Score)]]
  def scoreMotion(ngo: Ngo.Id, motion: Motion.Id, score: Score): F[Unit]
}

object Scoring {
  case class Score(value: Double) extends AnyVal
}
