package votelog.domain.crudi

import cats.Show
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

import scala.collection.SortedSet

trait ReadOnlyStoreAlg[F[_], T, Identity, Order] {
  type ReadParameters
  type IndexParameters

  def index(queryParameters: IndexParameters): F[Index[Identity]]
  def read(queryParameters: ReadParameters)(id: Identity): F[T]
}

object ReadOnlyStoreAlg {

  case class IndexQueryParameters[T, Ordering](pageSize: PageSize, offset: Offset, indexContext: T, orderings: List[Ordering])
  // todo: index should return an sorted set
  case class Index[T](totalEntities: Int, entities: List[T])

  object QueryParameters {
    case class PageSize(value: Int) { override def toString: String = value.toString }
    object PageSize {
      implicit val pageSizeOrdering: Ordering[PageSize] =
        (lhs: PageSize, rhs: PageSize) => lhs.value.compareTo(rhs.value)
      implicit val pageSizeShow: Show[PageSize] = _.value.toString
    }

    case class Offset private (value: Long) { override def toString: String = value.toString }
    object Offset {
      def apply(value: Long): Offset = {
        //assert(value <= 0, "offset must be equal to or larger than 0")
        new Offset(value)
      }
    }
  }

  sealed trait Error extends Exception
  object Error {
    case class InvalidId[T](message: String, value: T) extends Error { override def toString: String = message }
  }
}
