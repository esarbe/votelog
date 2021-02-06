package votelog.domain.politics

import java.time.LocalDate

case class Business(title: Option[String], description: Option[String], submittedBy: Option[String], submissionDate: LocalDate)

object Business {
  case class Id(value: Int)

  sealed trait Ordering extends Product
  object Ordering {
    case object Title extends Ordering
    case object SubmissionDate extends Ordering
    case object SubmittedBy extends Ordering

    val values = Set(Title, SubmissionDate, SubmittedBy)
    lazy val fromString: (String => Ordering) =
      (values zip values).map { case (key, value) => (key.toString, value)}.toMap.apply
  }
}
