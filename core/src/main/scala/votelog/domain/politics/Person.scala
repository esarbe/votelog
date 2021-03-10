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

case class PersonPartial(
  id: Option[Person.Id],
  firstName: Option[Person.Name],
  lastName: Option[Person.Name],
  canton: Option[Canton],
  gender: Option[Person.Gender],
  party: Option[String],
  dateOfElection: Option[java.time.LocalDate],
  dateOfBirth: Option[java.time.LocalDate]
)

object Person {

  case class Id(value: Int) extends AnyVal { override def toString: String = value.toString}
  case class Name(value: String) extends AnyVal

  object Name  {
    implicit val ordering: Ordering[Name] = (x: Name, y: Name) => x.value.compare(y.value)
  }

  sealed trait Gender extends Product with Serializable
  object Gender {
    case object Female extends Gender
    case object Male extends Gender
  }

  sealed trait Field extends Product with Serializable
  object Field {
    case object Id extends Field
    case object FirstName extends Field
    case object LastName extends Field
    case object Canton extends Field
    case object Gender extends Field
    case object Party extends Field
    case object DateOfElection extends Field
    case object DateOfBirth extends Field

    // ugly and brittle and fragile and broken: sequence must be equal to construction of select query ....
    // fixme: create better partial mechanism
    val values: List[Field] = List(Id, FirstName, LastName, Canton, Gender, Party, DateOfElection, DateOfBirth)
    lazy val fromString: String => Field =
      (values zip values).map { case (key, value) => (key.toString, value)}.toMap.apply
  }
}
