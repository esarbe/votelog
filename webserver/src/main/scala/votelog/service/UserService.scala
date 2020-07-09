package votelog.service

import cats.effect.IO
import io.circe.Encoder
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
) extends StoreService[User, User.Id, UserStore.Recipe] {
  override implicit val queryParamDecoder: param.Decoder[store.QueryParameters] = param.Decoder.always(Unit)
  override implicit val indexQueryParamDecoder: param.Decoder[store.IndexQueryParameters] = param.Decoder.always(Unit)
}
