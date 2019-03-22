package votelog.implementation

import cats.Applicative
import votelog.infrastructure.logging.Logger

class Log4SLogger[F[_]](log: org.log4s.Logger)(implicit A: Applicative[F]) extends Logger[F] {
  override def warn(message: String): F[Unit] = A.pure(log.warn(message))

  override def info(message: String): F[Unit] = A.pure(log.info(message))

  override def error(t: Throwable)(message: String): F[Unit] = A.pure(log.error(t)(message))

  override def error(message: String): F[Unit] = A.pure(log.error(message))

  override def debug(message: String): F[Unit] = A.pure(log.debug(message))

  override def debug(t: Throwable)(message: String): F[Unit] = A.pure(log.debug(t)(message))

}
