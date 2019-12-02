package votelog.domain.crudi

import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

trait ReadOnlyStoreAlg[F[_], T, Identity] {
  type QueryParameters
  type IndexQueryParameters

  def index(queryParameters: IndexQueryParameters): F[List[Identity]]
  def read(queryParameters: QueryParameters)(id: Identity): F[T]
}

object ReadOnlyStoreAlg {

  case class IndexQueryParameters[T](pageSize: PageSize, offset: Offset, queryParameters: T)

  object QueryParameters {
    case class PageSize(value: Int)
    case class Offset(value: Long)
  }

  sealed trait Error extends Exception
  object Error {
    case class InvalidId[T](message: String, value: T) extends Error { override def toString: String = message }
  }
}
