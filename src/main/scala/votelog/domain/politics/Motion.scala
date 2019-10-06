package votelog.domain.politics

case class Motion(name: String, submitter: Politician.Id)

object Motion {

  case class Id(value: Int)
}
