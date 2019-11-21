package votelog.domain.crudi

import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.politics.Language

trait ReadOnlyStoreAlg[F[_], T, Identity] {
  def index(queryParameters: IndexQueryParameters): F[List[Identity]]
  def read(queryParameters: QueryParameters)(id: Identity): F[T]
}

object ReadOnlyStoreAlg {
  // this could be a type variable in the ReadOnlyStoreAlg
  case class QueryParameters(language: Language, year: Int)

  // this could be a type variable in the ReadOnlyStoreAlg
  case class IndexQueryParameters(pageSize: PageSize, offset: Offset, queryParameters: QueryParameters)

  object QueryParameters {
    case class PageSize(value: Int)
    case class Offset(value: Long)
  }

  sealed trait Error extends Exception
  object Error {
    case class InvalidId[T](message: String, value: T) extends Error { override def toString: String = message }
  }
}
