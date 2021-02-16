package votelog.client.service

import votelog.client.Configuration
import votelog.domain.authentication.User
import votelog.domain.authorization.{Capability, Component}
import votelog.persistence.UserStore

import scala.concurrent.Future
import votelog.orphans.circe.implicits._
import votelog.domain.param.Encoder
import votelog.domain.param.Params


class UserStoreXhr(configuration: Configuration)
  extends StoreXhr[User, User.Id, UserStore.Recipe, User.Partial, User.Field, User.Field]
    with UserStore[Future] {

  val indexUrl =  configuration.url + "/user"

  override def findByName(name: String): Future[Option[User]] =
    Future.failed(???)

  override def grantPermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override def revokePermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override implicit val indexQueryParameterEncoder: Encoder[Set[User.Field]] = _ => Params.empty
  override implicit val queryParameterEncoder: Encoder[Set[User.Field]] = _ => Params.empty
}
