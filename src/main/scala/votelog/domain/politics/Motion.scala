package votelog.domain.politics

import java.util.UUID

case class Motion(name: String, submitter: Politician.Id)

object Motion {

  case class Id(value: UUID)
}
