package votelog.domain.politics

case class Person(
  id: Person.Id,
  firstName: Person.Name,
  lastName: Person.Name,
  canton: Canton,
  gender: Person.Gender,
  party: String,
  dateOfElection: Option[java.time.LocalDate],
  dateOfBirth: Option[java.time.LocalDate],
)

object Person {
  case class Id(value: Int) extends AnyVal { override def toString: String = value.toString}
  case class Name(value: String) extends AnyVal

  sealed trait Gender
  object Gender {
    case object Female extends Gender
    case object Male extends Gender
  }

  sealed trait Ordering extends Product with Serializable
  object Ordering {
    case object FirstName extends Ordering
    case object LastName extends Ordering
    case object Id extends Ordering
    case object DateOfBirth extends Ordering

    val values: Set[Ordering] = Set(FirstName, LastName, Id, DateOfBirth)
    lazy val fromString: String => Ordering =
      (values zip values).map { case (key, value) => (key.toString, value)}.toMap.apply
  }
}
