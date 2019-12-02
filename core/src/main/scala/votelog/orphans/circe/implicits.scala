package votelog.orphans.circe

import io.circe
import io.circe.{Decoder, KeyDecoder}
import votelog.domain.authentication.User
import votelog.domain.politics.Person.Gender
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics.{Language, LegislativePeriod, Motion, Ngo, Party, Person}

object implicits {

  implicit val partyIdCirceKeyDecoder: circe.KeyDecoder[Party.Id] = KeyDecoder.decodeKeyInt.map(Party.Id)
  implicit val motionIdCirceKeyDecoder: circe.KeyDecoder[Motion.Id] = KeyDecoder.decodeKeyInt.map(Motion.Id)
  implicit val personIdCirceKeyDecoder: circe.KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val userIdCirceKeyDecoder: circe.KeyDecoder[User.Id] = a => Some(User.Id(a))
  implicit val lpKeyDecoder: KeyDecoder[LegislativePeriod.Id] = KeyDecoder.decodeKeyInt.map(LegislativePeriod.Id)
  implicit val langKeyDecoder: KeyDecoder[Language] = (key: String) => Language.fromIso639_1(key)
  implicit val ngoIdCirceKeyDecoder: circe.KeyDecoder[Ngo.Id] = a => Some(Ngo.Id(a))

  implicit val personGenderDecoder: Decoder[Person.Gender] =
    Decoder.decodeString.map {
      case "female" => Female
      case "male" => Male
    }


}
