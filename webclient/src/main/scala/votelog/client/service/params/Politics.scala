package votelog.client.service.params

import io.circe.KeyEncoder
import votelog.domain.politics.{Context, Language}
import votelog.domain.param
import votelog.domain.param.Params

object Politics {

  // TODO: all these implicits should be moved to
  // votelog.orphans.service.params.Encoder

  implicit val contextParamEncoder: param.Encoder[Context] =
    (context: Context) =>
      Params(Map(
        "lang" -> Seq(context.language.iso639_1),
        "lp" -> Seq(context.legislativePeriod.value.toString)
      ))

  implicit val langParam: param.Encoder[Language] =
    (lang: Language) => Params(Map("lang" -> Seq(lang.iso639_1)))

  //implicit def orderEncoder[A](implicit ev: KeyEncoder[A]): param.Encoder[List[A]] =
    //(ordering: List[A]) => Params(Map("orderBy" -> ordering.map(ev.apply)))


}
