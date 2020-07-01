package votelog.client.service

import io.circe.{Decoder, parser}
import org.scalajs.dom.XMLHttpRequest

import scala.concurrent.Future

object AjaxRequest {
  def ifSuccess[T](f: XMLHttpRequest => Future[T])(res: XMLHttpRequest): Future[T] =  {
    if (200 <= res.status && res.status < 300) f(res)
    else Future.failed(new RuntimeException(res.responseText))
  }

  def fromJson[T: Decoder](res: XMLHttpRequest): Future[T] =
    parser.decode[T](res.responseText).fold(Future.failed, Future.successful)
}
