package votelog.domain.politics

case class Politician(id: Politician.Id, name: String, partyId: Party.Id)

object Politician {
  case class Id(value: Long)
}
