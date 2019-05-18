package votelog.domain.politics

case class Motion(id: Motion.Id, name: String, submitter: Politician.Id)

object Motion {
  case class Id(value: Long)
}
