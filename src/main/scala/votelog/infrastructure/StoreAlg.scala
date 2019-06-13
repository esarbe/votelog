package votelog
package infrastructure

// TODO: try to make Identity and Recipe inner types
trait StoreAlg[F[_], T, Identity, Recipe] {

  def index: F[List[Identity]]
  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, r: Recipe): F[T]
  def read(id: Identity): F[T]
}

