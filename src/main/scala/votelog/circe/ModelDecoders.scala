package votelog.circe

import io.circe
import io.circe.{Decoder, KeyDecoder}
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.authorization.{Capability, Component, User}
import votelog.domain.politics.{Motion, Ngo, Party, Person, Votum}

trait ModelDecoders {
  implicit val partyIdCirceDecoder: circe.Decoder[Party.Id] = Decoder.decodeInt.map(Party.Id)
  implicit val partyIdCirceKeyDecoder: circe.KeyDecoder[Party.Id] = KeyDecoder.decodeKeyInt.map(Party.Id)

  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id] = Decoder.decodeInt.map(Motion.Id)
  implicit val motionIdCirceKeyDecoder: circe.KeyDecoder[Motion.Id] = KeyDecoder.decodeKeyInt.map(Motion.Id)
  implicit val motionCirceDecoder: circe.Decoder[Motion] = deriveDecoder[Motion]

  implicit val personIdCirceDecoder: circe.Decoder[Person.Id] = Decoder.decodeInt.map(Person.Id)
  implicit val personIdCirceKeyDecoder: circe.KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val personCirceDecoder: circe.Decoder[Person] = deriveDecoder[Person]

  implicit val userIdCirceDecoder: circe.Decoder[User.Id] = deriveDecoder[User.Id]
  implicit val userIdCirceKeyDecoder: circe.KeyDecoder[User.Id] = KeyDecoder.decodeKeyUUID.map(User.Id)
  implicit val userEmailCirceDecoder: circe.Decoder[User.Email] = deriveDecoder[User.Email]

  implicit val capabilityCirceDecoder: circe.Decoder[Capability] = deriveDecoder[Capability]
  implicit val componentCirceDecoder: circe.Decoder[Component] = deriveDecoder[Component]
  implicit val userPermissionCirceDecoder: circe.Decoder[User.Permission] = deriveDecoder[User.Permission]
  implicit val userCirceDecoder: circe.Decoder[User] = deriveDecoder[User]

  implicit val ngoIdCirceDecoder: circe.Decoder[Ngo.Id] = deriveDecoder[Ngo.Id]
  implicit val ngoIdCirceKeyDecoder: circe.KeyDecoder[Ngo.Id] = KeyDecoder.decodeKeyUUID.map(Ngo.Id)
  implicit val nvoCirceDecoder: circe.Decoder[Ngo] = deriveDecoder[Ngo]

  implicit val votumCirceKeyDecoder: circe.KeyDecoder[Option[Votum]] = KeyDecoder.decodeKeyString.map(Votum.fromString)

}
