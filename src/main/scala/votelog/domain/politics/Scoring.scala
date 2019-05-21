package votelog.domain.politics

import votelog.domain.politics.Scoring.Score

trait Scoring[F[_]] {
  def politiciansScoredBy(ngo: Ngo): F[(Politician, Score)]
  def motionsScoredBy(ngo: Ngo): F[(Motion, Score)]
  def scorePolitician(ngo: Ngo, politician: Politician, score: Score): F[Score]
  def scoreMotion(ngo: Ngo, motion: Motion, score: Score): F[Score]
}

object Scoring {
  case class Score(value: Double) extends AnyVal
}
