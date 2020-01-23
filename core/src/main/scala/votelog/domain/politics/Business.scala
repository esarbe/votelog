package votelog.domain.politics

import java.time.LocalDate

case class Business(title: Option[String], description: Option[String], submittedBy: Option[String], submissionDate: LocalDate)

object Business {
  case class Id(value: Int)
}
