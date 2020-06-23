package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.{Business, Context, Language}
import votelog.infrastructure.{Param, ReadOnlyStoreService}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters

class BusinessService(
  val component: Component,
  val store: BusinessStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Business, Business.Id] {
  implicit val contextParamDecoder: Param[Context] = Params.contextParam
  override implicit val queryParamDecoder: Param[Language] = Params.languageParam
  override implicit val indexQueryParamDecoder: Param[IndexQueryParameters[Context]] = Params.indexQueryParam
}
