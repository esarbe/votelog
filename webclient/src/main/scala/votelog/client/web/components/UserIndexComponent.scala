package votelog.client.web.components

import mhtml.Rx
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.crudi.StoreAlg
import votelog.persistence.UserStore

import scala.concurrent.Future
import scala.xml.Node

class UserIndexComponent(
  override val store: UserStore[Future],
  defaultPageSize: PageSize,
) extends CrudIndexComponent[User, User.Id, UserStore.Recipe](store, defaultPageSize) {
  override def indexQueryParameters: Rx[store.IndexQueryParameters] = Rx(())
  override def queryParameters: Rx[store.QueryParameters] = Rx(())
  override def queryParametersView: Option[Node] = None
}
