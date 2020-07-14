package votelog.client.service

import io.circe._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.param.{Param, Params, Encoder => ParamEncoder}
import votelog.domain.param.Encoder._


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ReadOnlyStoreXhr[T: Decoder, Identity: Decoder: KeyEncoder](
  implicit indexDecoder: Decoder[Index[Identity]]
) extends ReadOnlyStoreAlg[Future, T, Identity]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: ParamEncoder[IndexQueryParameters]
  implicit val queryParameterBuilder: ParamEncoder[QueryParameters]

  def param(id: Identity): String = s"/${KeyEncoder[Identity].apply(id)}"

  override def index(queryParameters: IndexQueryParameters): Future[Index[Identity]] = {
    println(s"$indexUrl")
    Ajax.get(indexUrl + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[Index[Identity]](res.responseText).fold(Future.failed, Future.successful)
      }
  }


  override def read(queryParameters: QueryParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}

object ReadOnlyStoreXhr {

  import cats.implicits._

  implicit def indexQueryParam[T](implicit ev: ParamEncoder[T]): ParamEncoder[IndexQueryParameters[T]] =
    (qp: IndexQueryParameters[T]) => {
      val tParam = ev.encode(qp.queryParameters)
      Params(Map(
        "ps" -> Seq(qp.pageSize.value.toString),
        "os" -> Seq(qp.offset.value.toString)
      )) |+| tParam
    }
}