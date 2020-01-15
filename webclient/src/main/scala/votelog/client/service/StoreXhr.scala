package votelog.client.service

import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.StoreAlg
import cats.implicits._
import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class StoreXhr[T: Decoder, Identity: Decoder: KeyEncoder, Recipe: Encoder]
  extends StoreAlg[Future, T, Identity, Recipe]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: HttpQueryParameter[IndexQueryParameters]
  implicit val queryParameterBuilder: HttpQueryParameter[QueryParameters]

  def queryParam[T: HttpQueryParameter](t: T): String = HttpQueryParameter[T].encode(t)

  def param(id: Identity): String =
    s"/${KeyEncoder[Identity].apply(id)}"

  override def create(r: Recipe): Future[Identity] = {
    Ajax.post(indexUrl, r.asJson.noSpaces, withCredentials = true)
      .flatMap { res =>
        decode[Identity](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def delete(id: Identity): Future[Unit] = {
    Ajax.delete(indexUrl + param(id), withCredentials = true).void
  }

  override def update(id: Identity, r: Recipe): Future[T] = {
    Ajax.put(indexUrl + param(id), r.asJson.noSpaces, withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def index(queryParameters: IndexQueryParameters): Future[List[Identity]] = {
    Ajax.get(indexUrl + queryParam(queryParameters), withCredentials = true)
      .flatMap { res =>
        decode[List[Identity]](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def read(queryParameters: QueryParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + queryParam(queryParameters), withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}