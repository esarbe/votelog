package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction
import votelog.domain.data.Sorting.Direction.Descending
import votelog.domain.politics.Ngo
import votelog.infrastructure.StoreService
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.param

class NgoService(
  val component: Component,
  val store: NgoStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe, Ngo.Partial, Unit, IndexQueryParameters[Unit, Ngo.Field, Ngo.Field]] {
  override implicit val queryParamDecoder: param.Decoder[Unit] = param.Decoder.always(())
  override implicit val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Unit, Ngo.Field, Ngo.Field]] =
    param.Decoder.always(IndexQueryParameters(
      PageSize(10),
      Offset(0),
      (),
      List(Ngo.Field.Name -> Descending),
      Ngo.Field.values.toSet
    ))
}
