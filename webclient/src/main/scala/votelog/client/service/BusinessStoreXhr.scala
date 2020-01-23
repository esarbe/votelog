package votelog.client.service

import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Business, Context, Language}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._

import scala.concurrent.Future

class BusinessStoreXhr(configuration: Configuration)
  extends ReadOnlyStoreXhr[Business, Business.Id]
    with BusinessStore[Future] {

  override val indexUrl: String = configuration.url + "/business"

  implicit val contextParameterBuilder: HttpQueryParameter[Context] = params.Politics.contextParams
  override implicit val queryParameterBuilder: HttpQueryParameter[Language] = params.Politics.langParam
  override implicit val indexQueryParameterBuilder: HttpQueryParameter[ReadOnlyStoreAlg.IndexQueryParameters[Context]] =
    indexQueryParam
}
