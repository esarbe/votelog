package votelog.client.web.components

import votelog.domain.crudi.ReadOnlyStoreAlg

import scala.concurrent.Future

trait CrudShowComponent[T, Identity, Order] {
  val store: ReadOnlyStoreAlg[Future, T, Identity, Order]
}
