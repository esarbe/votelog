package votelog.client.service

import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.StoreAlg
import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class RestStore[T: Decoder, Identity: Decoder: KeyEncoder, Recipe: Encoder]
  extends StoreAlg[Future, T, Identity, Recipe]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: HttpQueryParameter[IndexQueryParameters]
  implicit val queryParameterBuilder: HttpQueryParameter[QueryParameters]

  def queryParam[T: HttpQueryParameter](t: T): String = HttpQueryParameter[T].encode(t)

  def location(id: Identity): String =
    s"/${KeyEncoder[Identity].apply(id)}"

  override def create(r: Recipe): Future[Identity] = {
    Ajax.post(indexUrl, r.asJson.noSpaces)
      .flatMap { res =>
        decode[Identity](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def delete(id: Identity): Future[Unit] = ???

  override def update(id: Identity, r: Recipe): Future[T] = {
    Ajax.put(indexUrl + location(id), r.asJson.noSpaces)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def index(queryParameters: IndexQueryParameters): Future[List[Identity]] = {
    Ajax.get(indexUrl + queryParam(queryParameters))
      .flatMap { res =>
        decode[List[Identity]](res.responseText).fold(Future.failed, Future.successful)
      }
  }

  override def read(queryParameters: QueryParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + location(id) + queryParam(queryParameters))
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}