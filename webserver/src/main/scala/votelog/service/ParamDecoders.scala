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
import votelog.domain.data.Sorting

object Params {

  val languageParam: Decoder[Language] = param.Decoder[Language]("lang")

  val contextParam: Decoder[Context] =
    (param.Decoder[LegislativePeriod.Id]("lp") zip languageParam)
      .map(Context.tupled)

  implicit def orderTupleKeyDecoder[Ordering: KeyDecoder]: KeyDecoder[(Ordering, Sorting.Direction)] =
    new KeyDecoder[(Ordering, Sorting.Direction)] {
      def apply(key: String): Option[(Ordering, Sorting.Direction)] = key.split('|').toList match {
        case ordering :: "Asc" :: Nil => KeyDecoder[Ordering].apply(ordering).map(o => (o, Sorting.Direction.Ascending))
        case ordering :: "Des" :: Nil => KeyDecoder[Ordering].apply(ordering).map(o => (o, Sorting.Direction.Descending))
        case _ => None
      }
  }

  def orderDecoder[Order: KeyDecoder]: Decoder[List[(Order, Sorting.Direction)]] =
    Decoder[List[(Order, Sorting.Direction)]]("orderBy")(listKeyDecoder[(Order, Sorting.Direction)])

  def indexParamsDecoder[T, Order, Field](
    contextDecoder: Decoder[T],
    orderDecoder: Decoder[List[(Order, Sorting.Direction)]],
    fieldsDecoder: Decoder[Set[Field]]
  ): Decoder[ReadOnlyStoreAlg.IndexQueryParameters[T, Order, Field]] = {

    implicit val pageSizeKeyDecoder: KeyDecoder[PageSize] = KeyDecoder.decodeKeyInt.map(PageSize.apply)
    implicit val offsetDecoder: KeyDecoder[Offset] = KeyDecoder.decodeKeyLong.map(Offset.apply)

    import param.Decoder._

    (Decoder[PageSize]("ps")
      zip Decoder[Offset]("os")
      zip contextDecoder
      zip orderDecoder
      zip fieldsDecoder)
      .map { case (((ps, os), t), order) =>
        IndexQueryParameters[T, Order, Field](ps, os, t, order, Set.empty)
    }
  }

}
