package votelog.domain.politics

import java.util.UUID

case class Politician(id: Politician.Id, name: String)

object Politician {

  case class Id(value: UUID)
}
