package votelog.persistence.doobie

import org.scalatest.FlatSpec
import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authorization.User
import votelog.domain.politics.Politician
import votelog.persistence.UserStore.{Password, Recipe}
import votelog.persistence.{PoliticianStore, StoreSpec, UserStore}

import scala.concurrent.ExecutionContext


class DoobieUserStoreSpec extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor =
    Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      s"jdbc:h2:mem:${getClass.getName};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "sa",
      "",
    )

  val hasher = new PasswordHasherAlg[IO] {
    override def hashPassword(password: String): IO[String] = IO.pure(s"hashed$password")
  }

  val store = new DoobieUserStore(transactor, hasher)
  val schema = new DoobieSchema(transactor)

  schema.initialize.unsafeRunSync()

  val creationRecipe: Recipe = UserStore.Recipe("name", User.Email("email"), Password.Clear("password"))
  val createdEntity: User.Id => User = _ => User("name", User.Email("email"), "hashedpassword", Set.empty)
  val updatedRecipe: Recipe = Recipe("new name", User.Email("new email"), Password.Clear("new password"))
  val updatedEntity: User.Id => User = _ => User("new name", User.Email("new email"), "hashednew password", Set.empty)

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
}
