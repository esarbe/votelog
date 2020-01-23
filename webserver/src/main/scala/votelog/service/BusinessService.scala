package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Business}
import votelog.infrastructure.{Param, ReadOnlyStoreService}
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._
import io.circe.generic.auto._

class MotionService(
  val component: Component,
  val store: BusinessStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Business, Business.Id] {
  override implicit val queryParamDecoder: Param[Context] = Params.contextParam
  override implicit val indexQueryParamDecoder: Param[ReadOnlyStoreAlg.IndexQueryParameters[Context]] =
    Params.indexQueryParam
}
