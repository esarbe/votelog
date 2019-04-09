package votelog.persistence.doobie

import doobie.util.Put
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

}
