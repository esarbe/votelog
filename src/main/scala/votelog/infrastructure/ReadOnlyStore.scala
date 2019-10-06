package votelog.infrastructure

trait ReadOnlyStoreAlg[F[_], T, Identity] {
  def index: F[List[Identity]]
  def read(id: Identity): F[T]
}
