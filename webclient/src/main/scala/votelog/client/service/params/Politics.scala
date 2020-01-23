package votelog.client.service.params

import votelog.client.service.HttpQueryParameter
import votelog.domain.politics.{Context, Language}

object Politics {
  implicit val contextParams: HttpQueryParameter[Context] =
    (context: Context) =>
      Map(
        "lang" -> context.language.iso639_1,
        "lp" -> context.legislativePeriod.value
      )
        .map { case (key, value) => s"$key=$value" }
        .mkString("&")

  implicit val langParam: HttpQueryParameter[Language] =
    (lang: Language) => s"lang=${lang.iso639_1}"
}
