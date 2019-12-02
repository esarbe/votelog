package votelog.domain.politics

import votelog.domain.politics.Person.{Canton, Gender, Name}

case class Person(
  id: Person.Id,
  firstName: Name,
  lastName: Name,
  canton: Canton,
  gender: Gender,
  party: String,
  dateOfElection: java.time.LocalDate,
  dateOfBirth: java.time.LocalDate,
)

object Person {
  case class Id(value: Int)
  case class Name(value: String)
  case class Canton(value: String)

  trait Gender
  object Gender {
    case object Female extends Gender
    case object Male extends Gender
  }

}
