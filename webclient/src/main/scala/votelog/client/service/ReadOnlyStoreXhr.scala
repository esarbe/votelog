package votelog.client.service

import io.circe._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import HttpQueryParameter._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ReadOnlyStoreXhr[T: Decoder, Identity: Decoder: KeyEncoder](
  implicit indexDecoder: Decoder[Index[Identity]]
) extends ReadOnlyStoreAlg[Future, T, Identity]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: HttpQueryParameter[IndexQueryParameters]
  implicit val queryParameterBuilder: HttpQueryParameter[QueryParameters]

  def param(id: Identity): String = s"/${KeyEncoder[Identity].apply(id)}"

  override def index(queryParameters: IndexQueryParameters): Future[Index[Identity]] = {
    println(s"$indexUrl")
    Ajax.get(indexUrl + "?" + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[Index[Identity]](res.responseText).fold(Future.failed, Future.successful)
      }
  }


  override def read(queryParameters: QueryParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + "?" + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}

object ReadOnlyStoreXhr {

  implicit def indexQueryParam[T: HttpQueryParameter]: HttpQueryParameter[IndexQueryParameters[T]] =
    (qp: IndexQueryParameters[T]) => {
      val tParam = implicitly[HttpQueryParameter[T]].encode(qp.queryParameters)
      Map(
        "ps" -> qp.pageSize.value,
        "os" -> qp.offset.value
      )
        .map { case (key, value) => s"$key=$value" }
        .mkString(
          "",
          "&",
          s"&$tParam")
    }
}