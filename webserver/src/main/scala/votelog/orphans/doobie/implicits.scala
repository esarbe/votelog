package votelog.orphans.doobie

import java.util.UUID

import doobie.util.{Put, Read}
import votelog.domain.authentication.User
import votelog.domain.politics.Person.Gender
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics.{Language, Votum}
import doobie.postgres.implicits._

object implicits {

  implicit val votumRead: Read[Votum] =
    Read[String].map(s =>
      // yes, this is unsafe. but I don't know a better way to deal with invalid
      // values from the DB. Votum could be represented as an Enum in
      // postgresql (https://github.com/esarbe/votelog/issues/3)
      Votum.fromString(s).getOrElse(sys.error(s"invalid string representation for votum: $s")))

  implicit val genderRead: Read[Gender] =
    Read[String].map {
      case "f" | "F" => Female
      case "m" | "M" => Male
    }

  implicit val languagePut: Put[Language] = Put[String].contramap(_.iso639_1.toUpperCase)
  implicit val languageRead: Read[Language] = Read[String].map(s => Language.fromIso639_1Unsafe(s.toLowerCase))

  implicit val votumPut: Put[Votum] =  Put[String].contramap(Votum.asString)
  implicit val UserIdPut: Put[User.Id] = Put[UUID].contramap(uid => UUID.fromString(uid.value))
  implicit val UserIdRead: Read[User.Id] = Read[UUID].map(v => User.Id(v.toString))
}
