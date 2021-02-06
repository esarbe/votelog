package votelog.client.service

import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.client.service.params.Politics._
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Business, Context, Language}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._
import votelog.domain.param.{Encoder => ParamEncoder}

import scala.concurrent.Future

class BusinessStoreXhr(configuration: Configuration)
  extends ReadOnlyStoreXhr[Business, Business.Id, Business.Ordering]
    with BusinessStore[Future] {

  override val indexUrl: String = configuration.url + "/business"

  override implicit val queryParameterBuilder: ParamEncoder[Language] = params.Politics.langParam
  override implicit val indexQueryParameterBuilder: ParamEncoder[ReadOnlyStoreAlg.IndexQueryParameters[Context, Business.Ordering]] = indexQueryParam
}
