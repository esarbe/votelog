package votelog.domain.politics

import votelog.infrastructure.encoding.Encoder

sealed trait Votum

object Votum {
  case object Yes extends Votum
  case object No extends Votum
  case object Abstain extends Votum
  case object Absent extends Votum


  implicit val votumEncoder: Encoder[String, Votum] = {
    case "yes"=> Right(Votum.Yes)
    case "no"=> Right(Votum.No)
    case "absent"=> Right(Votum.Absent)
    case "abstain"=> Right(Votum.Abstain)
    case unknown => Left(new RuntimeException(s"unknown value '$unknown'"))
  }

}
