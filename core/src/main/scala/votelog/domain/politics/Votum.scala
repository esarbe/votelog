package votelog.domain.politics

sealed trait Votum

object Votum {
  case object Yes extends Votum
  case object No extends Votum
  case object Abstain extends Votum
  case object Absent extends Votum

  val fromString: String => Option[Votum] = {
    case "Yes" => Some(Yes)
    case "No" => Some(No)
    case "Abstain" => Some(Abstain)
    case "Absent" => Some(Absent)
    case _ => None
  }

  val asString: Votum => String = {
    case Yes => "Yes"
    case No => "No"
    case Abstain => "Abstain"
    case Absent => "Absent"
  }


}
