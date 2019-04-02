package votelog
package infrastructure


trait CrudService[F[_], T] {
  type Recipe
  type Identity

  def index: F[List[Identity]]
  def create(r: Recipe): F[Identity]
  def delete(id: Identity): F[Unit]
  def update(id: Identity, t: T): F[T]
  def read(id: Identity): F[T]
}
