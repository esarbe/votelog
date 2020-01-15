package votelog.client.service

import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Business, Context}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._

import scala.concurrent.Future

class BusinessStoreXhr(configuration: Configuration)
  extends ReadOnlyStoreXhr[Business, Business.Id]
    with BusinessStore[Future] {

  override val indexUrl: String = configuration.url + "/business"

  override implicit val queryParameterBuilder: HttpQueryParameter[Context] = params.Politics.contextParams

  override implicit val indexQueryParameterBuilder: HttpQueryParameter[ReadOnlyStoreAlg.IndexQueryParameters[Context]] =
    indexQueryParam
}
