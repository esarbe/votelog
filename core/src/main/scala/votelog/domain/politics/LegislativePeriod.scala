package votelog.domain.politics

import java.time.LocalDate

import cats.Show
import votelog.domain.politics.LegislativePeriod.Id

import scala.collection.immutable

case class LegislativePeriod(id: Id, start: LocalDate, end: LocalDate)

object LegislativePeriod {
  case class Id(value: Int)
  object Id {
    implicit val legislativeIdOrdering: Ordering[Id] = (lhs: Id, rhs: Id) => lhs.value.compareTo(rhs.value)
    implicit val legislativePeriodShow: Show[Id] = _.value.toString
  }

  val Default: LegislativePeriod = LegislativePeriod(Id(50), LocalDate.of(2015, 11, 30), LocalDate.of(2019, 11, 25))

  val ids: immutable.Seq[Id] = (0 until 9).map(offset => Id(53 - offset))
}
