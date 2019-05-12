package votelog.persistence

import votelog.domain.authorization.User
import votelog.infrastructure.StoreAlg

trait UserStore[F[_]] extends StoreAlg[F, User, User.Id, UserStore.Recipe] {
  def findByName(name: String): F[Option[User]]
}

object UserStore {
  case class Recipe(name: String, email: User.Email)
}

