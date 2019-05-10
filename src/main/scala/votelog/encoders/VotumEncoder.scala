package votelog.encoders

import votelog.domain.politics.Votum
import votelog.infrastructure.encoding.Encoder

trait VotumEncoder {
  implicit val votumEncoder: Encoder[String, Votum] = {
    case "yes"=> Right(Votum.Yes)
    case "no"=> Right(Votum.No)
    case "absent"=> Right(Votum.Absent)
    case "abstain"=> Right(Votum.Abstain)
    case unknown => Left(new RuntimeException(s"unknown value '$unknown'"))
  }
}
