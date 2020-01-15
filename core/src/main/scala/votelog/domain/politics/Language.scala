package votelog.domain.politics

import cats.Show
import cats.syntax.show._

sealed trait Language extends Product with Serializable { def iso639_1: String }

object Language {
  case object German extends Language { def iso639_1 = "de" }
  case object English extends Language { def iso639_1 = "en" }
  case object French extends Language { def iso639_1 = "fr" }
  case object Italian extends Language { def iso639_1 = "it" }
  case object Romansh extends Language { def iso639_1 = "rm" }

  val values: Set[Language] = Set(German, English, French, Italian, Romansh)

  val fromIso639_1: String => Option[Language] = values.map(l => (l.iso639_1, l)).toMap.get _
  val fromIso639_1Unsafe: String => Language = values.map(l => (l.iso639_1, l)).toMap.apply
  implicit def languageOrdering: Ordering[Language] = (lhs: Language, rhs: Language) => rhs.show.compareTo(lhs.show)

  implicit val languageShow: Show[Language] = {
    case German => "Deutsch"
    case English => "English"
    case French =>  "Francais"
    case Italian => "Italiano"
    case Romansh => "Rumansch"
  }

}
