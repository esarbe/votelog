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
  case class Id(value: Int) extends AnyVal { override def toString: String = value.toString }
  case class Name(value: String) extends AnyVal

  sealed trait Gender extends Product with Serializable
  object Gender {
    case object Female extends Gender
    case object Male extends Gender

    val values: Set[Gender] = Set(Female, Male)
  }
}
