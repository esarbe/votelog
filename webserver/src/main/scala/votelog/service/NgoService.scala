package votelog.service

import cats.effect.IO
import io.circe.Encoder
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.{Business, Ngo}
import votelog.infrastructure.StoreService
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import io.circe.generic.auto._
import votelog.domain.Param
import votelog.domain.crudi.ReadOnlyStoreAlg.Index

class NgoService(
  val component: Component,
  val store: NgoStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe] {
  override implicit val queryParamDecoder: Param[store.QueryParameters] = Param.always(Unit)
  override implicit val indexQueryParamDecoder: Param[store.IndexQueryParameters] = Param.always(Unit)
}
