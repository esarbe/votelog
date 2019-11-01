package votelog.infrastructure

import votelog.infrastructure.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.infrastructure.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

trait ReadOnlyStoreAlg[F[_], T, Identity] {
  def index(queryParameters: IndexQueryParameters): F[List[Identity]]
  def read(queryParameters: QueryParameters)(id: Identity): F[T]
}

object ReadOnlyStoreAlg {
  case class QueryParameters(language: String)
  case class IndexQueryParameters(pageSize: PageSize, offset: Offset, queryParameters: QueryParameters)

  object QueryParameters {
    case class PageSize(value: Int)
    case class Offset(value: Int)
  }
}
