package votelog.domain.politics

import java.time.LocalDate

import votelog.domain.politics.LegislativePeriod.Id

case class LegislativePeriod(id: Id, start: LocalDate, end: LocalDate)

object LegislativePeriod {
  case class Id(value: Int)
  val Default: LegislativePeriod = LegislativePeriod(Id(50), LocalDate.of(2015, 11, 30), LocalDate.of(2019, 11, 25))
}
