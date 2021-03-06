package votelog.persistence.doobie

import cats.Id
import cats.effect.{ContextShift, IO}
import cats.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import votelog.app.Database
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authentication.User
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.crudi.StoreAlg
import votelog.domain.data.Sorting.Direction.Descending
import votelog.persistence.UserStore.{Password, Recipe}
import votelog.persistence.{StoreSpec, UserStore}

import scala.concurrent.ExecutionContext


class DoobieUserStoreSpec
  extends AnyFlatSpec
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
  val indexQueryParameters: IndexQueryParameters[Unit, User.Field, User.Field] =
    IndexQueryParameters(PageSize(10), Offset(0), (), List(User.Field.Name -> Descending), User.Field.values.toSet )
  val partialEntity = User.Partial(Some("name"), Some(User.Email("email")))

  val userStore =
    schema.initialize *>
      aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity, (a: User.Id) => partialEntity)((), indexQueryParameters)

  it should behave like userStore.unsafeRunSync()
}
