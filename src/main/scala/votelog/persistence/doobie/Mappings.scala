package votelog.persistence.doobie

import java.util.UUID

import doobie.util.{Put, Read}
import votelog.domain.authorization.User
import votelog.domain.politics.{Motion, Ngo, Party, Politician, Vote, Votum}

object Mappings {
  implicit val votumPut: Put[Votum] =
    Put[String].contramap(Votum.asString)

  implicit val votumRead: Read[Votum] =
    Read[String].map(s =>
      // yes, this is unsafe. but I don't know a better way to deal with invalid
      // values from the DB. Votum could be represented as an Enum in
      // postgresql (https://github.com/esarbe/votelog/issues/3)
      Votum.fromString(s).getOrElse(sys.error(s"invalid string representation for votum: $s")))

  implicit val MotionIdPut: Put[Motion.Id] = Put[String].contramap(_.value.toString)
  implicit val MotionIdRead: Read[Motion.Id] = Read[String].map(v => Motion.Id(UUID.fromString(v)))

  implicit val NgoIdPut: Put[Ngo.Id] = Put[String].contramap(_.value.toString)
  implicit val NgoIdRead: Read[Ngo.Id] = Read[String].map(v => Ngo.Id(UUID.fromString(v)))

  implicit val PoliticianIdPut: Put[Politician.Id] = Put[String].contramap(_.value.toString)
  implicit val PoliticianIdRead: Read[Politician.Id] = Read[String].map(v => Politician.Id(UUID.fromString(v)))

  implicit val UserIdPut: Put[User.Id] = Put[String].contramap(_.value.toString)
  implicit val UserIdRead: Read[User.Id] = Read[String].map(v => User.Id(UUID.fromString(v)))

  implicit val PartIdPut: Put[Party.Id] = Put[String].contramap(_.value.toString)
  implicit val PartyIdRead: Read[Party.Id] = Read[String].map(v => Party.Id(UUID.fromString(v)))

}
