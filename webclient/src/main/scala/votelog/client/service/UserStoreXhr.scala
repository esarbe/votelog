package votelog.client.service

import io.circe.Decoder
import votelog.client.Configuration
import votelog.domain.authentication.User
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.persistence.UserStore

import scala.concurrent.Future
import votelog.orphans.circe.implicits._
import votelog.domain.param.Encoder
import votelog.domain.param.Params


class UserStoreXhr(configuration: Configuration)
  extends StoreXhr[User, User.Id, UserStore.Recipe]
    with UserStore[Future] {

  val indexUrl =  configuration.url + "/user"

  override def findByName(name: String): Future[Option[User]] =
    Future.failed(???)

  override def grantPermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override def revokePermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override implicit val indexQueryParameterEncoder: Encoder[Unit] = _ => Params.empty
  override implicit val queryParameterEncoder: Encoder[Unit] = _ => Params.empty
}
