package votelog.circe

import io.circe
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.authorization.{Capability, Component, User}
import votelog.domain.politics.{Motion, Ngo, Party, Politician}

trait ModelDecoders {
  implicit val partyIdCirceDecoder: circe.Decoder[Party.Id] = Decoder.decodeLong.map(Party.Id)

  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id] = Decoder.decodeLong.map(Motion.Id)
  implicit val motionCirceDecoder: circe.Decoder[Motion] = deriveDecoder[Motion]

  implicit val politicianIdCirceDecoder: circe.Decoder[Politician.Id] = Decoder.decodeLong.map(Politician.Id)
  implicit val politicianCirceDecoder: circe.Decoder[Politician] = deriveDecoder[Politician]

  implicit val userIdCirceDecoder: circe.Decoder[User.Id] = deriveDecoder[User.Id]
  implicit val userEmailCirceDecoder: circe.Decoder[User.Email] = deriveDecoder[User.Email]
  implicit val capabilityCirceDecoder: circe.Decoder[Capability] = deriveDecoder[Capability]
  implicit val componentCirceDecoder: circe.Decoder[Component] = deriveDecoder[Component]
  implicit val userPermissionCirceDecoder: circe.Decoder[User.Permission] = deriveDecoder[User.Permission]
  implicit val userCirceDecoder: circe.Decoder[User] = deriveDecoder[User]

  implicit val ngoIdCirceDecoder: circe.Decoder[Ngo.Id] = deriveDecoder[Ngo.Id]
  implicit val nvoCirceDecoder: circe.Decoder[Ngo] = deriveDecoder[Ngo]
}
