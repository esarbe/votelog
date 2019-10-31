package votelog.domain.politics

import votelog.domain.politics.Scoring._

trait Scoring[F[_]] {
  def motionsScoredBy(ngo: Ngo.Id): F[List[(Motion.Id, Score)]]
  def scoreMotion(ngo: Ngo.Id, motion: Motion.Id, score: Score): F[Unit]
  def removeMotionScore(ngo: Ngo.Id, motion: Motion.Id): F[Unit]
}

object Scoring {
  case class Score(value: Double) extends AnyVal
  case class Weight(value: Double) extends AnyVal
}
