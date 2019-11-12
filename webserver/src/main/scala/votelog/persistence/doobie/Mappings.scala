package votelog.persistence.doobie


import java.util.UUID

import doobie.util.{Put, Read}
import votelog.domain.politics.{Motion, Ngo, Party, Person, Votum}
import doobie.postgres.implicits._
import votelog.domain.authentication.User

object Mappings {
  implicit val votumPut: Put[Votum] =
    Put[String].contramap(Votum.asString)

  implicit val votumRead: Read[Votum] =
    Read[String].map(s =>
      // yes, this is unsafe. but I don't know a better way to deal with invalid
      // values from the DB. Votum could be represented as an Enum in
      // postgresql (https://github.com/esarbe/votelog/issues/3)
      Votum.fromString(s).getOrElse(sys.error(s"invalid string representation for votum: $s")))

  implicit val MotionIdPut: Put[Motion.Id] = Put[Int].contramap(_.value)
  implicit val MotionIdRead: Read[Motion.Id] = Read[Int].map(v => Motion.Id(v))

  implicit val NgoIdPut: Put[Ngo.Id] = Put[String].contramap(nid => nid.value)
  implicit val NgoIdRead: Read[Ngo.Id] = Read[String].map(v => Ngo.Id(v))

  implicit val PoliticianIdPut: Put[Person.Id] = Put[Int].contramap(_.value)
  implicit val PoliticianIdRead: Read[Person.Id] = Read[Int].map(v => Person.Id(v))

  implicit val UserIdPut: Put[User.Id] = Put[UUID].contramap(uid => UUID.fromString(uid.value))
  implicit val UserIdRead: Read[User.Id] = Read[UUID].map(v => User.Id(v.toString))

  implicit val PartyIdRead: Read[Party.Id] = Read[Int].map(v => Party.Id(v))
}
