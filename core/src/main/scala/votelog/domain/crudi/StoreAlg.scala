package votelog.domain.crudi

// TODO: try to make Identity and Recipe inner types
trait StoreAlg[F[_], T, Identity, Recipe]
  extends ReadOnlyStoreAlg[F, T, Identity] {

  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, r: Recipe): F[T]
}

