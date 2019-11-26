package votelog.service

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, LegislativePeriod}
import votelog.infrastructure.Param
import votelog.orphans.circe.implicits._
import cats.implicits._
import io.circe.KeyDecoder
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.infrastructure.Param.combine

object Params {
  val contextParam: Param[Context] =
    combine(Param[LegislativePeriod.Id]("lp"), Param[Language]("lang"))
      .map(Context.tupled)

  def indexQueryParam[T](implicit ev: Param[T]): Param[ReadOnlyStoreAlg.IndexQueryParameters[T]] = {
    implicit val pageSizeKeyDecoder: KeyDecoder[PageSize] = KeyDecoder.decodeKeyInt.map(PageSize)
    implicit val offsetDecoder: KeyDecoder[Offset] = KeyDecoder.decodeKeyLong.map(Offset)

    combine(combine(Param[PageSize]("ps"), Param[Offset]("os")), ev)
        .map{ case (ps, os, t) => IndexQueryParameters[T](ps, os, t) }
  }

}
