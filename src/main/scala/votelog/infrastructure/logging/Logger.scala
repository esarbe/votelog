package votelog.infrastructure.logging

trait Logger[F[_]] {
  def warn(message: String): F[Unit]
  def info(message: String): F[Unit]
  def error(t: Throwable)(message: String): F[Unit]
  def error(message: String): F[Unit]
  def debug(t: Throwable)(message: String): F[Unit]
  def debug(message: String): F[Unit]
}
