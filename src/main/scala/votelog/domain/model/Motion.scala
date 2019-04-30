package votelog.domain.model

import votelog.infrastructure.Identified

case class Motion(id: Motion.Id, name: String, submitter: Politician.Id)

object Motion {
  case class Id(value: Long)

  implicit object MotionIdentified extends Identified[Motion] {
    override type Identity = Id

    override def identity(t: Motion): Id = t.id
  }
}
