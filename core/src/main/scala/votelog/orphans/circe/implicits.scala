package votelog.orphans.circe

import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.extras.encoding.UnwrappedEncoder
import shapeless.Lazy
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics._
//import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.defaults._
import votelog.domain.authentication.User.Permission
import votelog.domain.authorization.{Capability, Component}
import votelog.persistence.{NgoStore, UserStore}
import votelog.persistence.UserStore.Password

object implicits {

  implicit val keyEncoderUserId: KeyEncoder[User.Id] = KeyEncoder.encodeKeyString.contramap(_.value)
  implicit val userIdCirceKeyDecoder: KeyDecoder[User.Id] = a => Some(User.Id(a))
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
  implicit val personIdCirceKeyDecoder: KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val lpKeyDecoder: KeyDecoder[LegislativePeriod.Id] = KeyDecoder.decodeKeyInt.map(LegislativePeriod.Id.apply)
  implicit val langKeyDecoder: KeyDecoder[Language] = (key: String) => Language.fromIso639_1(key)

  implicit val businessCodec: Codec[Business] = deriveConfiguredCodec
  implicit val businessIdKeyDecoder: KeyDecoder[Business.Id] = KeyDecoder.decodeKeyInt.map(Business.Id)
  implicit val businessIdKeyEncoder: KeyEncoder[Business.Id] = KeyEncoder.encodeKeyInt.contramap(_.value)
  implicit val businessIdCodec: Codec[Business.Id] = deriveUnwrappedCodec

  implicit val votumCodec: Encoder[Votum] =
    Encoder.encodeString.contramap {
      case Votum.Yes => "yes"
      case Votum.No => "no"
      case Votum.Abstain => "abstain"
      case Votum.Absent => "absent"
    }

  implicit val businessVotumEncoder: Encoder[Map[Business.Id, Votum]] = Encoder.encodeMap

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

  implicit def indexCodec[T: Codec]: Codec[Index[T]] =
    new Codec[Index[T]] {
      override def apply(index: Index[T]): Json = Json.obj(
        ("totalEntities", Json.fromInt(index.totalEntities)),
        ("entities", Encoder.encodeList[T].apply(index.entities))
      )

      override def apply(c: HCursor): Result[Index[T]] =
        for {
          totalEntities <- c.downField("totalEntities").as[Int]
          entities <- c.downField("entities").as[List[T]]
        } yield Index(totalEntities, entities)
    }
}
