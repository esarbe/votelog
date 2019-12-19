package votelog.client.service

import votelog.domain.authentication.User
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.crudi.StoreAlg
import votelog.persistence.UserStore

import scala.concurrent.Future

class UserStoreRest extends UserStore[Future] {

  override def findByName(name: String): Future[Option[User]] = ???

  override def grantPermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] = ???

  override def revokePermission(userId: User.Id, component: Component, capability: Capability): Future[Unit] = ???

  override def create(r: UserStore.Recipe): Future[User.Id] = ???

  override def delete(id: User.Id): Future[Unit] = ???

  override def update(id: User.Id, r: UserStore.Recipe): Future[User] = ???

  override def index(queryParameters: Unit): Future[List[User.Id]] = ???

  override def read(queryParameters: Unit)(id: User.Id): Future[User] = ???
}
