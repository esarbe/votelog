package votelog.circe

import io.circe
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import votelog.domain.authorization.{Capability, Component, User}
import votelog.domain.politics.{Motion, Ngo, Party, Person, Votum}

trait ModelEncoders {
  implicit val politicianIdCirceEncoder: circe.Encoder[Person.Id] = Encoder.encodeInt.contramap(_.value)
  implicit val partyIdCirceEncoder: circe.Encoder[Party.Id] = Encoder.encodeInt.contramap(_.value)
  implicit val politicianCirceEncoder: circe.Encoder[Person] = deriveEncoder[Person]
  implicit val motionIdCirceEncoder: circe.Encoder[Motion.Id] = Encoder.encodeInt.contramap(_.value)
  implicit val motionCirceEncoder: circe.Encoder[Motion] = deriveEncoder[Motion]
  implicit val userIdCirceEncoder: circe.Encoder[User.Id] = Encoder.encodeUUID.contramap(_.value)
  implicit val capabilityEncode: circe.Encoder[Capability] =
    Encoder.encodeString.contramap(Capability.showComponent.show)

  implicit val componentCirceEncoder: circe.Encoder[Component] = Encoder.encodeString.contramap(_.location)

  implicit val permissionEncoder: circe.Encoder[User.Permission] = deriveEncoder[User.Permission]
  implicit val userEmailCirceEncoder: circe.Encoder[User.Email] = deriveEncoder[User.Email]
  implicit val userCirceEncoder: circe.Encoder[User] = deriveEncoder[User]


  implicit val votumCirceEncoder: circe.Encoder[Votum] =
    Encoder.encodeString.contramap {
      case Votum.Yes => "yes"
      case Votum.No => "no"
      case Votum.Abstain => "abstain"
      case Votum.Absent => "absent"
    }

  implicit val ngoCirceEncoder: circe.Encoder[Ngo] = deriveEncoder[Ngo]
  implicit val ngoIdCirceEncoder: circe.Encoder[Ngo.Id] = deriveEncoder[Ngo.Id]
}

