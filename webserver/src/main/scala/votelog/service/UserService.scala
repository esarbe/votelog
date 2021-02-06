package votelog.service

import cats.effect.IO
import votelog.domain.param
import votelog.orphans.circe.implicits._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.infrastructure.StoreService
import votelog.persistence.UserStore

class UserService(
  val component: Component,
  val store: UserStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[User, User.Id, UserStore.Recipe, User.Ordering] {
  override implicit val queryParamDecoder: param.Decoder[store.ReadParameters] = param.Decoder.always(())
  override implicit val indexQueryParamDecoder: param.Decoder[store.IndexParameters] = param.Decoder.always(())
}
