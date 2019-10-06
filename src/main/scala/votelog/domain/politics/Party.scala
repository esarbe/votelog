package votelog.domain.politics

case class Party(id: Party.Id, name: String)

object Party {
  case class Id(value: Int)
}
