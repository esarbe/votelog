package votelog.endpoint.client

import endpoints.algebra.Documentation
import endpoints.{Tupler, xhr}
import org.scalajs.dom.XMLHttpRequest

trait CorsEndpoints extends xhr.Endpoints {

  override def request[A, B, C, AB, Out](
    method: Method,
    url: Url[A],
    entity: RequestEntity[B],
    docs: Documentation,
    headers: RequestHeaders[C]
  )(implicit tuplerAB: Tupler.Aux[A, B, AB], tuplerABC: Tupler.Aux[AB, C, Out]): Request[Out] =
    new Request[Out] {
      def apply(abc: Out) = {
        val (ab, c) = tuplerABC.unapply(abc)
        val (a, b) = tuplerAB.unapply(ab)
        val xhr = makeXhr(method, url, a, headers, c)
        (xhr, Some(entity(b, xhr)))
      }

      def href(abc: Out) = {
        val (ab, _) = tuplerABC.unapply(abc)
        val (a, _) = tuplerAB.unapply(ab)
        url.encode(a)
      }
    }

  private def makeXhr[A, B](method: String, url: Url[A], a: A, headers: RequestHeaders[B], b: B): XMLHttpRequest = {
    val xhr = new XMLHttpRequest
    xhr.open(method, url.encode(a))
    xhr.withCredentials = true
    headers(b, xhr)
    xhr
  }
}