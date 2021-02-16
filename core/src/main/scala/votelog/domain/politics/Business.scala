package votelog.domain.politics

import java.time.LocalDate

case class Business(title: Option[String], description: Option[String], submittedBy: Option[String], submissionDate: LocalDate)

object Business {
  case class Partial(
    title: Option[String],
    description: Option[String],
    submittedBy: Option[String],
    submissionDate: Option[LocalDate],
  )

  val empty = Partial(None, None, None, None)

  case class Id(value: Int)

  sealed trait Field extends Product
  object Field {
    case object Title extends Field
    case object Description extends Field
    case object SubmittedBy extends Field
    case object SubmissionDate extends Field

    val values = List(Title, Description,  SubmittedBy, SubmissionDate)
    lazy val fromString: (String => Field) =
      (values zip values).map { case (key, value) => (key.toString, value)}.toMap.apply
  }
}
