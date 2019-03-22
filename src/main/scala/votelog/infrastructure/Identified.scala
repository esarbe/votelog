package votelog.infrastructure

trait Identified[T] {
  type Identity
  def identity(t: T): Identity
}
