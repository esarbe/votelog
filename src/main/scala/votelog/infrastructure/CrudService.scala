package votelog
package infrastructure


trait CrudService[F[_], T] {
  type Recipe
  implicit val I: Identified[T]

  def create(r: Recipe): F[T]
  def delete(id: I.Identity): F[T]
  def update(t: T): F[T]
  def read(id: I.Identity): F[T]
}
