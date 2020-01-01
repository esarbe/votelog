package votelog.client.web.components.ngo

import mhtml.Rx
import votelog.client.web.components.CrudIndexComponent
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics.Ngo
import votelog.persistence.NgoStore

import scala.concurrent.Future
import scala.xml.Node

class NgoIndexComponent(
  override val store: NgoStore[Future],
  defaultPageSize: PageSize,
) extends CrudIndexComponent[Ngo, Ngo.Id, NgoStore.Recipe](store, defaultPageSize) {
  override def indexQueryParameters: Rx[store.IndexQueryParameters] = Rx(())
  override def queryParameters: Rx[store.QueryParameters] = Rx(())
  override def queryParametersView: Option[Node] = None
}
