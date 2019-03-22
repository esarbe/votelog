package votelog.domain.model

import votelog.domain.model.Motion.{Id, MotionIdentified}
import votelog.infrastructure.Identified

case class Motion (id: Id)

object Motion {
  case class Id(value: Long) extends AnyVal

  implicit object MotionIdentified extends Identified[Motion] {
    override type Identity = Id

    override def identity(t: Motion): Id = t.id
  }
}
