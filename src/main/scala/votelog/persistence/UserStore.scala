package votelog.persistence

import votelog.domain.authorization.{Capability, Component, User}
import votelog.infrastructure.StoreAlg

trait UserStore[F[_]] extends StoreAlg[F, User, User.Id, UserStore.Recipe] {
  def findByName(name: String): F[Option[User]]
  def grantPermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
  def revokePermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
}

object UserStore {
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
    case class Clear(value: String) extends Password
    case class Hashed(value: String) extends Password
  }
}

