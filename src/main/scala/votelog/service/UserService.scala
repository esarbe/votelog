package votelog.service

import cats.effect.IO
import votelog.circe.implicits._
import votelog.domain.authorization.{AuthorizationAlg, Component, User}
import votelog.infrastructure.StoreService
import votelog.persistence.UserStore

class UserService(
  val component: Component,
  val store: UserStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[User, User.Id, UserStore.Recipe] {

}
