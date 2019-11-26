package votelog.service

import java.time.Instant
import java.time.format.DateTimeFormatter

import cats._
import cats.implicits._
import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, LegislativePeriod, Motion}
import votelog.infrastructure.{Param, ReadOnlyStoreService}
import votelog.persistence.MotionStore
import votelog.orphans.circe.implicits._

class MotionService(
  val component: Component,
  val store: MotionStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Motion, Motion.Id] {
  override implicit val queryParamDecoder: Param[Context] = Params.contextParam
  override implicit val indexQueryParamDecoder: Param[ReadOnlyStoreAlg.IndexQueryParameters[Context]] =
    Params.indexQueryParam
}
