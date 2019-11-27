package votelog.persistence.doobie

import cats.effect.{ContextShift, IO}
import cats.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.app.Database
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authentication.User
import votelog.persistence.UserStore.{Password, Recipe}
import votelog.persistence.{StoreSpec, UserStore}

import scala.concurrent.ExecutionContext


class DoobieUserStoreSpec
  extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val hasher = new PasswordHasherAlg[IO] {
    override def hashPassword(password: String): IO[String] = IO.pure(s"hashed$password")
  }

  val schema = new DoobieSchema(transactor)
  val store = new DoobieUserStore(transactor, hasher)

  val creationRecipe: Recipe = UserStore.Recipe("name", User.Email("email"), Password.Clear("password"))
  val createdEntity: User.Id => User = _ => User("name", User.Email("email"), "hashedpassword", Set.empty)
  val updatedRecipe: Recipe = Recipe("new name", User.Email("new email"), Password.Clear("new password"))
  val updatedEntity: User.Id => User = _ => User("new name", User.Email("new email"), "hashednew password", Set.empty)

  val userStore =
    schema.initialize *>
      aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)((), ())

  it should behave like userStore.unsafeRunSync()
}
