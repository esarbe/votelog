package votelog.domain.politics

import java.time.LocalDate

case class Business(title: String, description: String, submittedBy: String, submissionDate: LocalDate)

object Business {
  case class Id(value: Int)
}
