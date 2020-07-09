package votelog.client.service.params

import votelog.domain.politics.{Context, Language}
import votelog.domain.param
import votelog.domain.param.Params

object Politics {
  implicit val contextParamEncoder: param.Encoder[Context] =
    (context: Context) =>
      Params(Map(
        "lang" -> Seq(context.language.iso639_1),
        "lp" -> Seq(context.legislativePeriod.value.toString)
      ))

  implicit val langParam: param.Encoder[Language] =
    (lang: Language) => Params(Map("lang" -> Seq(lang.iso639_1)))
}
