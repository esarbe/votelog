package votelog.domain.authentication

import votelog.domain.authentication.Authentication.Credentials

trait AuthenticationAlg[F[_]] {
  def login(cred: Credentials): F[Either[String, User]]
}

object Authentication {

  trait Credentials
  object Credentials {
    case class UserPassword(username: String, password: String) extends Credentials
  }

}
