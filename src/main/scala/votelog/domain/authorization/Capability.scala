package votelog.domain.authorization

import cats.Show

sealed trait Capability

object Capability {
  case object Read extends Capability
  case object Create extends Capability
  case object Update extends Capability
  case object Delete extends Capability

  implicit object showComponent extends Show[Capability] {
    override def show(c: Capability): String = c match {
      case Read => "Read"
      case Create => "Create"
      case Update => "Update"
      case Delete => "Delete"
    }
  }
}
