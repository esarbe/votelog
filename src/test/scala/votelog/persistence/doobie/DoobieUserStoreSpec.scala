package votelog.persistence.doobie

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authorization.User
import votelog.persistence.UserStore.{Password, Recipe}
import votelog.persistence.{StoreSpec, UserStore}

import scala.concurrent.ExecutionContext


class DoobieUserStoreSpec extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  val hasher = new PasswordHasherAlg[IO] {
    override def hashPassword(password: String): IO[String] = IO.pure(s"hashed$password")
  }

  val store = new DoobieUserStore(transactor, hasher)

  val creationRecipe: Recipe = UserStore.Recipe(UserStore.newId, "name", User.Email("email"), Password.Clear("password"))
  val createdEntity: User.Id => User = _ => User("name", User.Email("email"), "hashedpassword", Set.empty)
  val updatedRecipe: Recipe = Recipe(UserStore.newId, "new name", User.Email("new email"), Password.Clear("new password"))
  val updatedEntity: User.Id => User = _ => User("new name", User.Email("new email"), "hashednew password", Set.empty)

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
}
