package votelog.domain.politics

import java.util.UUID

case class Ngo(name: String)

object Ngo {

  case class Id(value: UUID)
}
