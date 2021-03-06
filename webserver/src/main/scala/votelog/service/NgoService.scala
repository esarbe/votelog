package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.data.Sorting.Direction
import votelog.domain.politics.Ngo
import votelog.infrastructure.StoreService
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.param

class NgoService(
  val component: Component,
  val store: NgoStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe, Ngo.Partial, Unit, Seq[(Ngo.Field, Direction)]] {
  override implicit val queryParamDecoder: param.Decoder[Unit] = param.Decoder.always(())
  override implicit val indexQueryParamDecoder: param.Decoder[Seq[(Ngo.Field, Direction)]] = param.Decoder.always(Seq.empty)
}
