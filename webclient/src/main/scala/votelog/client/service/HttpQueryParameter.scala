package votelog.client.service

import votelog.domain.politics.Context

/**
 * TODO: check if/how to reuse the Param infrastructure from votelog.webserver
  */
trait HttpQueryParameter[T] {
  def encode(t: T): String
}

object HttpQueryParameter {

  implicit final class HttpQueryParameterOps[T: HttpQueryParameter](t: T) {
    def urlEncode: String = HttpQueryParameter[T].encode(t)
  }

  @inline def apply[T](implicit ev: HttpQueryParameter[T]): ev.type = ev
}