package votelog.domain.politics

case class Politician(id: Politician.Id, name: String)

object Politician {
  case class Id(value: Long)
}
