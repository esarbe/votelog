package votelog.orphans.doobie

import cats.{Applicative, Apply}

import java.util.UUID
import doobie.util.{Put, Read}
import votelog.domain.authentication.User
import votelog.domain.politics.Person.{Field, Gender}
import votelog.domain.politics.Person.Gender.{Female, Male}
import votelog.domain.politics.{Language, Ngo, Person, Votum}
import doobie.postgres.implicits._
import doobie.util.meta.Meta
import votelog.orphans.doobie.domain.politics.PersonInstances

object implicits extends PersonInstances {

  implicit val votumRead: Read[Votum] =
    Read[String].map(s =>
      // yes, this is unsafe. but I don't know a better way to deal with invalid
      // values from the DB. Votum could be represented as an Enum in
      // postgresql (https://github.com/esarbe/votelog/issues/3)
      Votum.fromString(s).getOrElse(sys.error(s"invalid string representation for votum: $s")))

  implicit val languagePut: Put[Language] = Put[String].contramap(_.iso639_1.toUpperCase)
  implicit val languageRead: Read[Language] = Read[String].map(s => Language.fromIso639_1Unsafe(s.toLowerCase))

  implicit val votumPut: Put[Votum] =  Put[String].contramap(Votum.asString)
  implicit val UserIdPut: Put[User.Id] = Put[UUID].contramap(uid => UUID.fromString(uid.value))
  implicit val UserIdRead: Read[User.Id] = Read[UUID].map(v => User.Id(v.toString))

  implicit val ngoIdPut: Put[Ngo.Id] = Put[UUID].contramap(uid => UUID.fromString(uid.value))
  implicit val ngoIdRead: Read[Ngo.Id] = Read[UUID].map(v => Ngo.Id(v.toString))

  implicit val languageMeta: Meta[Language] =
    Meta[String].imap {
      case "EN" => Language.English
      case "FR" => Language.French
      case "RM" => Language.Romansh
      case "IT" => Language.Italian
      case "DE" => Language.German
    } {
      case Language.English => "EN"
      case Language.French => "FR"
      case Language.Romansh => "RM"
      case Language.Italian => "IT"
      case Language.German => "DE"
    }
}
