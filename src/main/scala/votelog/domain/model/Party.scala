package votelog.domain.model

case class Party(id: Party.Id, name: String)

object Party {
  case class Id(value: Long)
}
