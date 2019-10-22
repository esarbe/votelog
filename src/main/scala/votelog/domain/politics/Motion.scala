package votelog.domain.politics

case class Motion(name: String, submitter: Person.Id)

object Motion {

  case class Id(value: Int)
}
