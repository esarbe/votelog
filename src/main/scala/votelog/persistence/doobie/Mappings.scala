package votelog.persistence.doobie

import doobie.util.{Put, Read}
import votelog.domain.politics.Votum

object Mappings {
  implicit val votumPut: Put[Votum] =
    Put[String].contramap(Votum.asString)

  implicit val votumRead: Read[Votum] =
    Read[String].map(s =>
      // yes, this is unsafe. but I don't know a better way to deal with invalid
      // values from the DB. Votum could be represented as an Enum in
      // postgresql (https://github.com/esarbe/votelog/issues/3)
      Votum.fromString(s).getOrElse(sys.error(s"invalid string representation for votum: $s")))
}
