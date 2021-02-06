package votelog.service

import cats.effect.IO
import io.circe.Encoder
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Ngo
import votelog.infrastructure.StoreService
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.param

class NgoService(
  val component: Component,
  val store: NgoStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe, Ngo.Ordering] {
  override implicit val queryParamDecoder: param.Decoder[store.ReadParameters] = param.Decoder.always(())
  override implicit val indexQueryParamDecoder: param.Decoder[store.IndexParameters] = param.Decoder.always(())
}
