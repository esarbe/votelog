package votelog.service

import cats.effect.IO
import votelog.domain.param
import votelog.orphans.circe.implicits._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting
import votelog.domain.data.Sorting.Direction
import votelog.domain.data.Sorting.Direction.Descending
import votelog.infrastructure.StoreService
import votelog.persistence.UserStore

class UserService(
  val component: Component,
  val store: UserStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[
  User,
  User.Id,
  UserStore.Recipe,
  User.Partial,
  Unit,
  IndexQueryParameters[Unit, User.Field, User.Field]
] {
  override implicit val queryParamDecoder: param.Decoder[()] = param.Decoder.always(Set.empty)
  override implicit val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Unit, User.Field, User.Field]] =
    param.Decoder.always(
      IndexQueryParameters(
        pageSize = PageSize(10),
        offset = Offset(0),
        indexContext = (),
        orderings = List(User.Field.Name -> Descending),
        fields = User.Field.values.toSet)
    )
}
