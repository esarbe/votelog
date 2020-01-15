package votelog.client.service

import io.circe._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.politics.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ReadOnlyStoreXhr[T: Decoder, Identity: Decoder: KeyEncoder]
  extends ReadOnlyStoreAlg[Future, T, Identity]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: HttpQueryParameter[IndexQueryParameters]
  implicit val queryParameterBuilder: HttpQueryParameter[QueryParameters]

  def encode[P: HttpQueryParameter](p: P): String = HttpQueryParameter[P].encode(p)

  def param(id: Identity): String =
    s"/${KeyEncoder[Identity].apply(id)}"


  override def index(queryParameters: IndexQueryParameters): Future[List[Identity]] = {
    Ajax.get(indexUrl + "?" + encode(queryParameters), withCredentials = true)
      .flatMap { res =>
        decode[List[Identity]](res.responseText).fold(Future.failed, Future.successful)
      }
  }


  override def read(queryParameters: QueryParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + "?" + encode(queryParameters), withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}

object ReadOnlyStoreXhr {

  def indexQueryParam[T: HttpQueryParameter](qp: IndexQueryParameters[T]): String = {
    val tParam = implicitly[HttpQueryParameter[T]].encode(qp.queryParameters)
    Map(
      "ps" -> qp.pageSize.value,
      "os" -> qp.offset.value
    )
      .map { case (key, value) => s"$key=$value" }
      .mkString("", "&", s"&$tParam")
  }
}