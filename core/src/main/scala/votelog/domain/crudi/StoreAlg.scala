package votelog.domain.crudi

import cats.Id

// TODO: try to make Identity and Recipe inner types
trait StoreAlg[F[_], T, Identity, Recipe, Partial, Ordering, Fields]
  extends ReadOnlyStoreAlg[F, T, Identity, Partial, Ordering, Fields] {

  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, r: Recipe): F[T]
}

