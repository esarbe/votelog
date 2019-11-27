package votelog.service

import cats.effect.IO
import votelog.orphans.circe.implicits._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.infrastructure.{Param, StoreService}
import votelog.persistence.UserStore

class UserService(
  val component: Component,
  val store: UserStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[User, User.Id, UserStore.Recipe] {

  override implicit val queryParamDecoder: Param[store.QueryParameters] = Param.always(Unit)
  override implicit val indexQueryParamDecoder: Param[store.IndexQueryParameters] = Param.always(Unit)
}
