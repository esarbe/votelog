package votelog.persistence


import cats._
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import votelog.domain.authentication.User
import votelog.domain.authorization.{Capability, Component}
import votelog.domain.crudi.StoreAlg

trait UserStore[F[_]] extends StoreAlg[F, User, User.Id, UserStore.Recipe] {
  def findByName(name: String): F[Option[User]]
  def grantPermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
  def revokePermission(userId: User.Id, component: Component, capability: Capability): F[Unit]
}

object UserStore {
  def newId[F[_]: Sync: Functor]: F[User.Id] = FUUID.randomFUUID[F].map(User.Id)

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
}

