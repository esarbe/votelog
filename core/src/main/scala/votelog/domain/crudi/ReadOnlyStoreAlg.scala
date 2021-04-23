package votelog.domain.crudi

import cats.Show
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction

trait ReadOnlyStoreAlg[F[_], T, Identity, Partial, ReadParameters, IndexParameters] {
  def index(queryParameters: IndexParameters): F[Index[Identity, Partial]]
  def read(queryParameters: ReadParameters)(id: Identity): F[T]
}

object ReadOnlyStoreAlg {

  case class IndexQueryParameters[T, Ordering, Field](pageSize: PageSize, offset: Offset, indexContext: T, orderings: List[(Ordering, Direction)], fields: Set[Field])

  case class Index[Id, Partial](totalEntities: Int, entities: List[(Id, Partial)])

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
