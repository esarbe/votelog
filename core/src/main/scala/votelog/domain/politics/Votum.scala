package votelog.domain.politics

sealed trait Votum

object Votum {
  case object Yes extends Votum
  case object No extends Votum
  case object Abstain extends Votum
  case object Absent extends Votum

  val fromInt: Int => Option[Votum] = {
    case 1 => Some(Yes)
    case 2 => Some(No)
    case 3 => Some(Abstain)
    case 5 => Some(Absent)
    case _ => None
  }

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
