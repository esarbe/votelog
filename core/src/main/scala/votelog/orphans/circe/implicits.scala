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
  implicit val userOrderingKeyDecoder: KeyDecoder[User.Ordering] =
    KeyDecoder.decodeKeyString.map(User.Ordering.fromString)

  implicit val personIdCodec: Codec[Person.Id] = deriveUnwrappedCodec
  implicit val personNameCodec: Codec[Person.Name] = deriveUnwrappedCodec
  implicit val passwordClearCodec: Codec[Password.Clear] = deriveUnwrappedCodec
  implicit val cantonCodec: Codec[Canton] = deriveUnwrappedCodec
  implicit val userStoreRecipeCodec: Codec[UserStore.Recipe] = deriveConfiguredCodec

  implicit val ngoCodec: Codec[Ngo] = deriveConfiguredCodec
  implicit val ngoIdCodec: Codec[Ngo.Id] = deriveUnwrappedCodec
  implicit val ngoStoreRecipeCodec: Codec[NgoStore.Recipe] = deriveConfiguredCodec
  implicit val ngoIdCirceKeyDecoder: KeyDecoder[Ngo.Id] = KeyDecoder.decodeKeyString.map(Ngo.Id)
  implicit val ngoOrderingKeyDecoder: KeyDecoder[Ngo.Ordering] =
    KeyDecoder.decodeKeyString.map(Ngo.Ordering.fromString)

  implicit val keyEncoderNgoId: KeyEncoder[Ngo.Id] = KeyEncoder.encodeKeyString.contramap(_.value)

  implicit val partyIdCirceKeyDecoder: KeyDecoder[Party.Id] = KeyDecoder.decodeKeyInt.map(Party.Id)
  implicit val personIdCirceKeyDecoder: KeyDecoder[Person.Id] = KeyDecoder.decodeKeyInt.map(Person.Id)
  implicit val personIdCirceKeyEncoder: KeyEncoder[Person.Id] = KeyEncoder.encodeKeyInt.contramap(_.value)
  implicit val lpKeyDecoder: KeyDecoder[LegislativePeriod.Id] = KeyDecoder.decodeKeyInt.map(LegislativePeriod.Id.apply)
  implicit val langKeyDecoder: KeyDecoder[Language] = (key: String) => Language.fromIso639_1(key)

  implicit val businessCodec: Codec[Business] = deriveConfiguredCodec
  implicit val businessIdKeyDecoder: KeyDecoder[Business.Id] = KeyDecoder.decodeKeyInt.map(Business.Id)
  implicit val businessIdKeyEncoder: KeyEncoder[Business.Id] = KeyEncoder.encodeKeyInt.contramap(_.value)
  implicit val businessIdCodec: Codec[Business.Id] = deriveUnwrappedCodec

  implicit val businessOrderingKeyDecoder: KeyDecoder[Business.Ordering] =
    KeyDecoder.decodeKeyString.map(Business.Ordering.fromString)
  implicit val businessOrderingKeyEncoder: KeyEncoder[Business.Ordering] =
    KeyEncoder.encodeKeyString.contramap(_.toString)

  implicit val personOrderingKeyDecoder: KeyDecoder[Person.Ordering] =
    KeyDecoder.decodeKeyString.map(Person.Ordering.fromString)
  implicit val personOrderingKeyEncoder: KeyEncoder[Person.Ordering] =
    KeyEncoder.encodeKeyString.contramap(_.toString)

  implicit val votumCodec: Codec[Votum] = Codec.from(
    Decoder.decodeString.map {
      case "yes" => Votum.Yes
      case "no" => Votum.No
      case "abstain" => Votum.Abstain
      case "absent" => Votum.Absent
    },
    Encoder.encodeString.contramap[Votum] {
      case Votum.Yes => "yes"
      case Votum.No => "no"
      case Votum.Abstain => "abstain"
      case Votum.Absent => "absent"
    }
  )

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
