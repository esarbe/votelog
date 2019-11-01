package votelog.crypto

import doobie.scalatest.IOChecker
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}
import org.scalatest._
import Matchers._
import cats.Id
import cats.effect.IO
import votelog.crypto.PasswordHasherJavaxCrypto.Salt

class PasswordHasherJavaxCryptoSpec
  extends WordSpec
    with ScalaFutures
    with GeneratorDrivenPropertyChecks {

  "PasswordHasherJavaxCrypto" should {
    "salt and hash a given password" in forAll { (salt: String, password: String) =>

      whenever(salt.nonEmpty && password.nonEmpty) {
        val aHashedPassword = new PasswordHasherJavaxCrypto[IO](Salt(salt)).hashPassword(password)

        aHashedPassword.unsafeRunSync() shouldNot be(salt)
        aHashedPassword.unsafeRunSync() shouldNot be(password)
      }
    }

    "fail if the given salt is empty" in {
      a[Exception] shouldBe thrownBy {
        new PasswordHasherJavaxCrypto[IO](Salt("")).hashPassword("password").unsafeRunSync()
      }
    }

    "give different hashes for different salts" in forAll { (a: String, b: String) =>
      val aHashedPassword = new PasswordHasherJavaxCrypto[IO](Salt(a)).hashPassword("password")
      val bHashedPassword = new PasswordHasherJavaxCrypto[IO](Salt(b)).hashPassword("password")

      whenever(a != b && a.nonEmpty && b.nonEmpty) {
        aHashedPassword.unsafeRunSync() shouldNot be(bHashedPassword.unsafeRunSync())
      }
    }
  }
}
