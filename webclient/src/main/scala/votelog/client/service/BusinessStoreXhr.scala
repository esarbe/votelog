package votelog.client.service

import cats.implicits.catsSyntaxSemigroup
import io.circe.KeyEncoder
import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.client.service.params.Politics._
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.data.Sorting
import votelog.domain.politics.{Business, Context, Language}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._
import votelog.domain.param.{Params, Encoder => ParamEncoder}

import scala.concurrent.Future

class BusinessStoreXhr(configuration: Configuration)
  extends ReadOnlyStoreXhr[Business, Business.Id, Business.Partial, Language, IndexQueryParameters[Context, Business.Field, Business.Field]]
    with BusinessStore[Future] {

  override val indexUrl: String = configuration.url + "/business"

  override implicit val queryParameterBuilder: ParamEncoder[Language] = params.Politics.langParam
  override implicit val indexQueryParameterBuilder: ParamEncoder[IndexParameters] =
    new ParamEncoder[ReadOnlyStoreAlg.IndexQueryParameters[Context, Business.Field, Business.Field]] {
      override def encode(qp: ReadOnlyStoreAlg.IndexQueryParameters[Context, Business.Field, Business.Field]): Params = {
        Params(Map(
          "ps" -> Seq(qp.pageSize.value.toString),
          "os" -> Seq(qp.offset.value.toString),
          "fields" -> qp.fields.map(KeyEncoder[Business.Field].apply),
        )) |+| orderEncoder(qp.orderings)
      }
    }
}
