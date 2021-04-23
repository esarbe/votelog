package votelog.client.service

import cats.Id
import io.circe._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.data.Sorting
import votelog.domain.param.{Params, Encoder => ParamEncoder}
import votelog.domain.param.Encoder._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ReadOnlyStoreXhr[T, Identity: Decoder: KeyEncoder, Partial, ReadParameters, IndexParameters](
  implicit indexDecoder: Decoder[Index[Identity, Partial]],
  implicit val entityDecoder: Decoder[T],
) extends ReadOnlyStoreAlg[Future, T, Identity, Partial, ReadParameters, IndexParameters]{

  val indexUrl: String // TODO: maybe reuse [[Component]]?!!!
  implicit val indexQueryParameterBuilder: ParamEncoder[IndexParameters]
  implicit val queryParameterBuilder: ParamEncoder[ReadParameters]

  def param(id: Identity): String = s"/${KeyEncoder[Identity].apply(id)}"

  override def index(queryParameters: IndexParameters): Future[Index[Identity, Partial]] = {
    println(s"$indexUrl")
    Ajax.get(indexUrl + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[Index[Identity, Partial]](res.responseText).fold(Future.failed, Future.successful)
      }
  }


  override def read(queryParameters: ReadParameters)(id: Identity): Future[T] = {
    Ajax.get(indexUrl + param(id) + queryParameters.urlEncode, withCredentials = true)
      .flatMap { res =>
        decode[T](res.responseText).fold(Future.failed, Future.successful)
      }
  }
}

object ReadOnlyStoreXhr {

  import cats.implicits._

  implicit def indexQueryParam[T, Ordering, Fields](
    implicit ev: ParamEncoder[T],
    ev1: ParamEncoder[List[(Ordering, Sorting.Direction)]]
  ): ParamEncoder[IndexQueryParameters[T, Ordering, Fields]] =
    (qp: IndexQueryParameters[T, Ordering, Fields]) => {
      val tParam = ev.encode(qp.indexContext)
      val oParam = ev1.encode(qp.orderings)
      Params(Map(
        "ps" -> Seq(qp.pageSize.value.toString),
        "os" -> Seq(qp.offset.value.toString)
      )) |+| tParam |+| oParam
    }
}