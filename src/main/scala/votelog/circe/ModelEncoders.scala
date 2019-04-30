package votelog.circe

import io.circe
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import votelog.domain.model.{Motion, Party, Politician, Votum}

trait ModelEncoders {
  implicit val politicianIdCirceEncoder: circe.Encoder[Politician.Id] = Encoder.encodeLong.contramap(_.value)
  implicit val partyIdCirceEncoder: circe.Encoder[Party.Id] = Encoder.encodeLong.contramap(_.value)
  implicit val politicianCirceEncoder: circe.Encoder[Politician] = deriveEncoder[Politician]
  implicit val motionIdCirceEncoder: circe.Encoder[Motion.Id] = Encoder.encodeLong.contramap(_.value)
  implicit val motionCirceEncoder: circe.Encoder[Motion] = deriveEncoder[Motion]
  implicit val votumCirceEncoder: circe.Encoder[Votum] =
    Encoder.encodeString.contramap {
      case Votum.Yes => "yes"
      case Votum.No => "no"
      case Votum.Abstain => "abstain"
      case Votum.Absent => "absent"
    }
}
