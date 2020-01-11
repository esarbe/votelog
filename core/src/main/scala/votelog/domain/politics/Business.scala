package votelog.domain.politics

case class Business(name: String, submitter: Person.Id)

object Business {
  case class Id(value: Int)
}
