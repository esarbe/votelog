package votelog.infrastructure

abstract class QueryParameterExtractor[T](implicit ev: Param[T]) {
  def unapply(params: Map[String, Iterable[String]]): Option[T] =
    ev.decode(params)
}