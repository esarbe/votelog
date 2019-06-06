package votelog.domain.politics

import java.util.UUID

case class Party(id: Party.Id, name: String)

object Party {

  case class Id(value: UUID)
}
