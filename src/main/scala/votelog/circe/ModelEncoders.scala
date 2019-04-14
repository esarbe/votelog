package votelog.circe

import io.circe
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import votelog.domain.model.{Motion, Politician, Votum}

trait ModelEncoders {
  implicit val politicianIdCirceEncoder: circe.Encoder[Politician.Id] = Encoder.encodeLong.contramap(_.value)
  implicit val politicianCirceEncoder: circe.Encoder[Politician] = deriveEncoder[Politician]
  implicit val motionIdCirceEncoder: circe.Encoder[Motion.Id] = Encoder.encodeLong.contramap(_.value)
  implicit val motionCirceEncoder: circe.Encoder[Motion] = deriveEncoder[Motion]
  implicit val votumCirceEncoder: circe.Encoder[Votum] = deriveEncoder[Votum]
}

