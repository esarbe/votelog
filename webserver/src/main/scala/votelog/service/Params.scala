package votelog.service

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, LegislativePeriod}
import votelog.orphans.circe.implicits._
import cats.implicits._
import io.circe.KeyDecoder
import votelog.domain.param
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.param.Decoder.combine

object Params {
  val languageParam: param.Decoder[Language] = param.Decoder[Language]("lang")

  val contextParam: param.Decoder[Context] =
    combine(param.Decoder[LegislativePeriod.Id]("lp"), languageParam)
      .map(Context.tupled)

  def indexQueryParam[T](implicit ev: param.Decoder[T]): param.Decoder[ReadOnlyStoreAlg.IndexQueryParameters[T]] = {
    implicit val pageSizeKeyDecoder: KeyDecoder[PageSize] = KeyDecoder.decodeKeyInt.map(PageSize.apply)
    implicit val offsetDecoder: KeyDecoder[Offset] = KeyDecoder.decodeKeyLong.map(Offset.apply)

    combine(combine(param.Decoder[PageSize]("ps"), param.Decoder[Offset]("os")), ev)
        .map { case (ps, os, t) => IndexQueryParameters[T](ps, os, t) }
  }

}
