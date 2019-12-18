package votelog.orphans.circe

import io.circe._
import votelog.domain.authentication.User
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics._
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.defaults._

object implicits {

  implicit val personIdCodec: Codec[Person.Id] = deriveUnwrappedCodec
  implicit val personNameCodec: Codec[Person.Name] = deriveUnwrappedCodec
  implicit val cantonCodec: Codec[Canton] = deriveUnwrappedCodec

  implicit val partyIdCirceKeyDecoder: KeyDecoder[Party.Id] = KeyDecoder.decodeKeyInt.map(Party.Id)
  implicit val motionIdCirceKeyDecoder: KeyDecoder[Motion.Id] = KeyDecoder.decodeKeyInt.map(Motion.Id)
  implicit val personIdCirceKeyDecoder: KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val userIdCirceKeyDecoder: KeyDecoder[User.Id] = a => Some(User.Id(a))
  implicit val lpKeyDecoder: KeyDecoder[LegislativePeriod.Id] = KeyDecoder.decodeKeyInt.map(LegislativePeriod.Id)
  implicit val langKeyDecoder: KeyDecoder[Language] = (key: String) => Language.fromIso639_1(key)
  implicit val ngoIdCirceKeyDecoder: KeyDecoder[Ngo.Id] = a => Some(Ngo.Id(a))

  implicit val personGenderDecoder: Decoder[Person.Gender] =
    Decoder.decodeString.map {
      case "female" => Female
      case "male" => Male
    }

  implicit val personGenderEncoder: Encoder[Person.Gender] =
    Encoder.encodeString.contramap {
      case Female => "female"
      case Male => "male"
    }
}
