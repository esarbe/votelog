package votelog.crypto

import cats.effect.Sync
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import votelog.crypto.PasswordHasherJavaxCrypto.Salt

class PasswordHasherJavaxCrypto[F[_]: Sync](
  salt: Salt,
) extends PasswordHasherAlg[F] {

  val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")

  def hashPassword(password: String): F[String] =
    Sync[F].delay {
      import javax.crypto.spec.PBEKeySpec
      import java.util.Base64

      val spec = new PBEKeySpec(password.toCharArray, salt.value.getBytes("UTF-8"), 65536,  128)
      Base64.getEncoder.encodeToString(factory.generateSecret(spec).getEncoded)
    }
}

object PasswordHasherJavaxCrypto {
  case class Salt(value: String) extends AnyVal
}
