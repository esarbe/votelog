package votelog.client.service

import votelog.client.Configuration
import votelog.domain.authentication.User
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.data.Sorting.Direction
import votelog.persistence.UserStore

import scala.concurrent.Future
import votelog.orphans.circe.implicits._
import votelog.domain.param.Encoder
import votelog.domain.param.Params


class UserStoreXhr(configuration: Configuration)
  extends StoreXhr[User, User.Id, UserStore.Recipe, User.Partial, Unit, IndexQueryParameters[Unit, User.Field, User.Field]]
    with UserStore[Future] {

  val indexUrl =  configuration.url + "/user"

  override def findByName(name: String): Future[Option[User]] =
    Future.failed(???)

  override def grantPermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override def revokePermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] =
    Future.failed(???)

  override implicit val indexQueryParameterEncoder: Encoder[IndexQueryParameters[Unit, User.Field, User.Field]] = Encoder.unit
  override implicit val queryParameterEncoder: Encoder[Unit] = Encoder.unit
}
