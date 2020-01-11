package votelog.orphans.circe

import io.circe._
import votelog.domain.authentication.User
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics._
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.defaults._
import votelog.domain.authentication.User.Permission
import votelog.domain.authorization.{Capability, Component}
import votelog.persistence.{NgoStore, UserStore}
import votelog.persistence.UserStore.Password

object implicits {

  implicit val keyEncoderUserId: KeyEncoder[User.Id] = KeyEncoder.encodeKeyString.contramap(_.value)
  implicit val userIdDecoder: Codec[User.Id] = deriveUnwrappedCodec
  implicit val userEmailCodec: Codec[User.Email] = deriveUnwrappedCodec
  implicit val componentCodec: Codec[Component] = deriveConfiguredCodec
  implicit val capabilityCodec: Codec[Capability] = deriveConfiguredCodec
  implicit val permissionCodec: Codec[Permission] = deriveConfiguredCodec
  implicit val userCodec: Codec[User] = deriveConfiguredCodec
  implicit val personIdCodec: Codec[Person.Id] = deriveUnwrappedCodec
  implicit val personNameCodec: Codec[Person.Name] = deriveUnwrappedCodec
  implicit val passwordClarCodec: Codec[Password.Clear] = deriveUnwrappedCodec
  implicit val cantonCodec: Codec[Canton] = deriveUnwrappedCodec
  implicit val userStoreRecipleCodec: Codec[UserStore.Recipe] = deriveConfiguredCodec

  implicit val ngoCodec: Codec[Ngo] = deriveConfiguredCodec
  implicit val ngoIdCodec: Codec[Ngo.Id] = deriveUnwrappedCodec
  implicit val ngoStoreRecipeCodec: Codec[NgoStore.Recipe] = deriveConfiguredCodec
  implicit val ngoIdCirceKeyDecoder: KeyDecoder[Ngo.Id] = KeyDecoder.decodeKeyString.map(Ngo.Id)
  implicit val keyEncoderNgoId: KeyEncoder[Ngo.Id] = KeyEncoder.encodeKeyString.contramap(_.value)

  implicit val partyIdCirceKeyDecoder: KeyDecoder[Party.Id] = KeyDecoder.decodeKeyInt.map(Party.Id)
  implicit val motionIdCirceKeyDecoder: KeyDecoder[Business.Id] = KeyDecoder.decodeKeyInt.map(Business.Id)
  implicit val personIdCirceKeyDecoder: KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val userIdCirceKeyDecoder: KeyDecoder[User.Id] = a => Some(User.Id(a))
  implicit val lpKeyDecoder: KeyDecoder[LegislativePeriod.Id] = KeyDecoder.decodeKeyInt.map(LegislativePeriod.Id)
  implicit val langKeyDecoder: KeyDecoder[Language] = (key: String) => Language.fromIso639_1(key)


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
