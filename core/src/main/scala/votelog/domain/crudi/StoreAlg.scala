package votelog.domain.crudi

// TODO: try to make Identity and Recipe inner types
trait StoreAlg[F[_], T, Identity, Recipe, Ordering]
  extends ReadOnlyStoreAlg[F, T, Identity, Ordering] {

  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, r: Recipe): F[T]
}

