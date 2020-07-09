package votelog.infrastructure

import votelog.domain.param
import votelog.domain.param.Params

abstract class QueryParameterExtractor[T](implicit ev: param.Decoder[T]) {
  def unapply(params: Map[String, Iterable[String]]): Option[T] = ev.decode(Params(params))
}
