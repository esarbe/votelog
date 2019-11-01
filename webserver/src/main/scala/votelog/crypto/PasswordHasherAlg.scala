package votelog.crypto

trait PasswordHasherAlg[F[_]] {
  def hashPassword(password: String): F[String]
}
