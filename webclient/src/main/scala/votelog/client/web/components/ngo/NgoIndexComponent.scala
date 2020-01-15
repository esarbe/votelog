package votelog.client.web.components.ngo

import mhtml.Rx
import votelog.client.web.components.CrudIndexComponent
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics.Ngo
import votelog.persistence.NgoStore

import scala.concurrent.Future
import scala.xml.Node

class NgoIndexComponent(
  val store: NgoStore[Future],
  val defaultPageSize: PageSize,
) extends CrudIndexComponent[Ngo, Ngo.Id]{
  val indexQueryParameters: Rx[store.IndexQueryParameters] = Rx(())
  val queryParameters: Rx[store.QueryParameters] = Rx(())
  val queryParametersView: Option[Node] = None
}
