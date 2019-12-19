package votelog.domain.authentication

import votelog.domain.authentication.Authentication.Credentials

trait SessionService[F[_]] {
  def login(cred: Credentials): F[Either[SessionService.Error, User]]
  def logout(): F[Unit]
}

object SessionService {
  sealed trait Error extends Exception
  object Error {
    case class DecodingError(source: Throwable) extends Error {
      override def getMessage: String = source.getMessage
    }
    case class ServiceError(source: Throwable) extends Error {
      override def getMessage: String = source.getMessage
    }
    case object AuthenticationFailed extends Error
  }

}

object Authentication {

  trait Credentials
  object Credentials {
    case class UserPassword(username: String, password: String) extends Credentials
  }
}
