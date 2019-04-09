package votelog.persistence

trait Schema[F[_]] {
  def initialize: F[Unit]
}
