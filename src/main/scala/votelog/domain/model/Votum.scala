package votelog.domain.model

sealed trait Votum

object Votum {
  case object Yes extends Votum
  case object No extends Votum
  case object Abstain extends Votum
  case object Absent extends Votum
}
