package votelog.domain.politics

import votelog.domain.politics.Person.{Gender, Name}

case class Person(
  id: Person.Id,
  firstName: Name,
  lastName: Name,
  canton: Canton,
  gender: Gender,
  party: String,
  dateOfElection: Option[java.time.LocalDate],
  dateOfBirth: Option[java.time.LocalDate],
)

object Person {
  case class Id(value: Int)
  case class Name(value: String)

  sealed trait Gender
  object Gender {
    case object Female extends Gender
    case object Male extends Gender
  }
}
