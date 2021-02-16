package votelog.service

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, LegislativePeriod}
import votelog.orphans.circe.implicits._
import cats.implicits._
import io.circe.KeyDecoder
import votelog.domain.param
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.param.Decoder.listKeyDecoder
import param.Decoder
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters

object Params {

  val languageParam: Decoder[Language] = param.Decoder[Language]("lang")

  val contextParam: Decoder[Context] =
    (param.Decoder[LegislativePeriod.Id]("lp") zip languageParam)
      .map(Context.tupled)

  def orderDecoder[Order: KeyDecoder]: Decoder[List[Order]] =
    Decoder[List[Order]]("orderBy")(listKeyDecoder[Order])

  def indexParamsDecoder[T, Order, Fields](
    contextDecoder: Decoder[T],
    orderDecoder: Decoder[List[Order]]
  ): Decoder[ReadOnlyStoreAlg.IndexQueryParameters[T, Order, Fields]] = {

    implicit val pageSizeKeyDecoder: KeyDecoder[PageSize] = KeyDecoder.decodeKeyInt.map(PageSize.apply)
    implicit val offsetDecoder: KeyDecoder[Offset] = KeyDecoder.decodeKeyLong.map(Offset.apply)

    import param.Decoder._

    (Decoder[PageSize]("ps")
      zip Decoder[Offset]("os")
      zip contextDecoder
      zip orderDecoder)
      .map { case (((ps, os), t), order) =>
        IndexQueryParameters[T, Order, Fields](ps, os, t, order, Set.empty)
    }
  }

}
