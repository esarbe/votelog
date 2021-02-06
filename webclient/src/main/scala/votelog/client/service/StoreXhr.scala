package votelog.client.service

import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.StoreAlg
import cats.implicits._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.param.{Encoder => ParamEncoder}
import votelog.domain.param.Encoder._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class StoreXhr[T: Decoder, Identity: Decoder: KeyEncoder, Recipe: Encoder, Ordering](
  implicit indexDecoder: Decoder[Index[Identity]]
) extends StoreAlg[Future, T, Identity, Recipe, Ordering]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterEncoder: ParamEncoder[IndexParameters]
  implicit val queryParameterEncoder: ParamEncoder[ReadParameters]

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
        decode[T](res.responseText).fold(Future.failed(_), Future.successful(_))
      }
  }

  override def index(queryParameters: IndexParameters): Future[Index[Identity]] = {
    Ajax.get(indexUrl + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[Index[Identity]](res.responseText).fold(Future.failed(_), Future.successful(_))
      }
  }

  override def read(queryParameters: ReadParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed(_), Future.successful(_))
      }
  }
}