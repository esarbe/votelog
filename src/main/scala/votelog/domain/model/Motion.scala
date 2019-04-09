package votelog.domain.model

import votelog.domain.model.Motion.{Id, MotionIdentified}
import votelog.infrastructure.Identified

import scala.util.Try

case class Motion(id: Motion.Id, name: String, submitter: Politician.Id)

object Motion {
  case class Id(value: Long)
  case class Recipe(name: String, submitter: Politician.Id)

  implicit object MotionIdentified extends Identified[Motion] {
    override type Identity = Id

    override def identity(t: Motion): Id = t.id
  }
}
