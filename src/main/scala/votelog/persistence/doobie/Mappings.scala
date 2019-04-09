package votelog.persistence.doobie

import doobie.util.{Put, Read}
import votelog.domain.model.Votum

object Mappings {
  implicit val votumPut: Put[Votum] =
    Put[String]
      .contramap {
        case Votum.Yes => "yes"
        case Votum.No => "no"
        case Votum.Abstain => "abstrain"
        case Votum.Absent => "absent"
      }

  implicit val votumRead: Read[Votum] =
    Read[String].map {
      case "yes" => Votum.Yes
      case "no" => Votum.No
      case "abstrain" => Votum.Abstain
      case "absent" => Votum.Absent
    }

}
