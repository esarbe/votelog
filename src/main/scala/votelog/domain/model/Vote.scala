package votelog.domain.model

import votelog.domain.model.Motion.Id
import votelog.infrastructure.Identified

import scala.util.Try

case class Vote(
  motionId: Motion.Id,
  politicianId: Politician.Id,
  votum: Votum
)

object Vote {

  implicit object VoteIdentified extends Identified[Vote] {
    override type Identity = (Motion.Id, Politician.Id)

    override def identity(t: Vote): Identity = (t.motionId, t.politicianId)
  }
}