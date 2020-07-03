package votelog.service

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, LegislativePeriod}
import votelog.orphans.circe.implicits._
import cats.implicits._
import io.circe.KeyDecoder
import votelog.domain.Param
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.infrastructure.Param.combine

object Params {
  val languageParam: Param[Language] = Param[Language]("lang")

  val contextParam: Param[Context] =
    combine(Param[LegislativePeriod.Id]("lp"), languageParam)
      .map(Context.tupled)

  def indexQueryParam[T](implicit ev: Param[T]): Param[ReadOnlyStoreAlg.IndexQueryParameters[T]] = {
    implicit val pageSizeKeyDecoder: KeyDecoder[PageSize] = KeyDecoder.decodeKeyInt.map(PageSize.apply)
    implicit val offsetDecoder: KeyDecoder[Offset] = KeyDecoder.decodeKeyLong.map(Offset.apply)

    combine(combine(Param[PageSize]("ps"), Param[Offset]("os")), ev)
        .map { case (ps, os, t) => IndexQueryParameters[T](ps, os, t) }
  }

}
