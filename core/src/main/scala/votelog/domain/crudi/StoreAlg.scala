package votelog.domain.crudi

import cats.Id

// TODO: try to make Identity and Recipe inner types
trait StoreAlg[F[_], T, Identity, Recipe, Partial, ReadParameters, IndexParameters]
  extends ReadOnlyStoreAlg[F, T, Identity, Partial, ReadParameters, IndexParameters] {

  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, r: Recipe): F[T]
}

