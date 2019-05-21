package votelog.domain.politics

import java.util.UUID

case class Ngo(id: Ngo.Id, name: String)

object Ngo {

  case class Id(value: UUID)
}
