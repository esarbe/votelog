package votelog.persistence


import java.util.UUID

import cats.implicits._
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}
import votelog.domain.authentication.User
import votelog.domain.authentication.User.Email
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.crudi.StoreAlg

trait UserStore[F[_]] extends StoreAlg[F, User, User.Id, UserStore.Recipe] {

  type QueryParameters = Unit
  type IndexQueryParameters = Unit

  def findByName(name: String): F[Option[User]]
  def grantPermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
  def revokePermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
}

object UserStore {
  def newId: User.Id = User.Id(UUID.randomUUID.toString)

  case class Recipe(name: String, email: User.Email, password: Password.Clear) {
    def prepare(passwordHash: Password.Hashed): PreparedRecipe =
      PreparedRecipe(name, email, passwordHash)
  }

  case class PreparedRecipe(name: String, email: User.Email, password: Password.Hashed)

  trait Password {
    val value: String
    override def toString: String = value
  }

  object Password {

    case class Clear(value: String) extends Password {
      override def toString: String = "[password in clear]"
    }

    case class Hashed(value: String) extends Password
  }

  def validateRecipe(
    name: String,
    email: Email,
    password: Password.Clear,
    confirmPassword: Password.Clear
  ): Validated[NonEmptyList[(String, String)], Recipe] = {

    def nonEmptyString(name: String): String => Validated[NonEmptyList[(String, String)], String] = (s: String) =>
      if (s.isEmpty) Invalid(name -> "must not be empty").toValidatedNel
      else Valid(s).toValidatedNel

    def areEqual(password: Password.Clear, confirmPassword: Password.Clear): Validated[NonEmptyList[(String, String)], Password.Clear] =
      if (password != confirmPassword) Invalid("confirmPassword" -> s"must be equal to password").toValidatedNel
      else Valid(password).toValidatedNel

    (nonEmptyString("name")(name),
      nonEmptyString("email")(email.value),
      nonEmptyString("password")(password.value) *>
        areEqual(password, confirmPassword)
    )
        .mapN { case (name, email, password) => Recipe(name, User.Email(email), password) }
  }
}

