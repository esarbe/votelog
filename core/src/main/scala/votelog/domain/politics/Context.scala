package votelog.domain.politics

case class Context(year: Int, language: Language)

sealed trait Language { def iso639_1: String }
object Language {
  case object German extends Language { def iso639_1 = "de" }
  case object English extends Language { def iso639_1 = "en" }
  case object French extends Language { def iso639_1 = "fr" }
  case object Italian extends Language { def iso639_1 = "it" }
  case object Romansh extends Language { def iso639_1 = "rm" }
}
