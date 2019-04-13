package votelog.circe

import io.circe
import io.circe.generic.semiauto.deriveEncoder
import votelog.domain.model.{Motion, Politician, Votum}

trait ModelEncoders {
  implicit val politicianIdEncoder: circe.Encoder[Politician.Id] = deriveEncoder[Politician.Id]
  implicit val politicianCirceEncoder: circe.Encoder[Politician] = deriveEncoder[Politician]
  implicit val motionIdCirceEncoder: circe.Encoder[Motion.Id] = deriveEncoder[Motion.Id]
  implicit val motionCirceEncoder: circe.Encoder[Motion] = deriveEncoder[Motion]
  implicit val votumCirceEncoder: circe.Encoder[Votum] = deriveEncoder[Votum]
}

