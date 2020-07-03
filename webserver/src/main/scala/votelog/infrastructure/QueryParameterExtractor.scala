package votelog.infrastructure

import votelog.domain.Param

abstract class QueryParameterExtractor[T](implicit ev: Param[T]) {
  def unapply(params: Map[String, Iterable[String]]): Option[T] = ev.decode(params)
}
