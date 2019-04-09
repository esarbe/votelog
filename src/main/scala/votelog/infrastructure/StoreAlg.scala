package votelog
package infrastructure


trait StoreAlg[F[_], T, Identity, Recipe] {

  def index: F[List[Identity]]
  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, t: T): F[T]
  def read(id: Identity): F[T]
}

