package votelog.circe

import io.circe
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.model.{Motion, Party, Politician}

trait ModelDecoders {
  implicit val partyIdCirceDecoder: circe.Decoder[Party.Id] = Decoder.decodeLong.map(Party.Id)

  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id] = Decoder.decodeLong.map(Motion.Id)
  implicit val motionCirceDecoder: circe.Decoder[Motion] = deriveDecoder[Motion]

  implicit val politicianIdCirceDecoder: circe.Decoder[Politician.Id] = Decoder.decodeLong.map(Politician.Id)
  implicit val politicianCirceDecoder: circe.Decoder[Politician] = deriveDecoder[Politician]
}
