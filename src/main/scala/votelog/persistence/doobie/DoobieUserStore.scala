package votelog.persistence.doobie

import java.util.UUID

import cats._
import cats.free.Free
import cats.implicits._
import doobie.free.connection
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.Meta
import votelog.crypto.PasswordHasherAlg
import votelog.domain.authorization.{Capability, Component, User}
import votelog.persistence.UserStore
import votelog.persistence.UserStore.{Password, PreparedRecipe, Recipe}
import votelog.persistence.doobie.Mappings._

class DoobieUserStore[F[_]: Monad](
  transactor: doobie.util.transactor.Transactor[F],
  passwordHasher: PasswordHasherAlg[F],
) extends UserStore[F] {

  implicit val metaCapability: Meta[Capability] =
    Meta[String]
      .imap {
        case "Read" => Capability.Read: Capability
        case "Create" => Capability.Create: Capability
        case "Update" => Capability.Update: Capability
        case "Delete" => Capability.Delete: Capability
      }{
        case Capability.Read => "Read"
        case Capability.Create => "Create"
        case Capability.Update => "Update"
        case Capability.Delete => "Delete"
      }

  implicit val readComponent: Meta[Component] =
    Meta[String]
      .imap(Component.apply)(_.location)


  private def readQuery(id: User.Id): ConnectionIO[User] = {
    val selectUser =
      sql"select name, email, passwordhash from users where id=$id"
        .query[(String, String, String)]
        .unique

    val selectPermissions =
      sql"select capability, component from permissions where userid = $id"
        .query[User.Permission]
        .accumulate[List]

    for {
      (name, email, hashedPassword) <- selectUser
      permissions <- selectPermissions
    } yield User(name, User.Email(email), hashedPassword, permissions.toSet)
  }


  private def deleteQuery(id: User.Id): doobie.ConnectionIO[Int] =
    sql"delete from users where id = ${id}"
      .update.run

  private def updateQuery(id: User.Id, recipe: PreparedRecipe) =
    sql"""
         |update users set
         |name = ${recipe.name},
         |email = ${recipe.email},
         |passwordhash = ${recipe.password}
         |where id = $id
         |"""
      .stripMargin.update.run

  private def insertQuery(recipe: PreparedRecipe, id: User.Id) = {
    sql"""
         |insert into users (id, name, email, passwordhash)
         |values (${id}, ${recipe.name}, ${recipe.email}, ${recipe.password})"""
      .stripMargin
      .update
      .run
  }


  def findIdByNameQuery(name: String): doobie.ConnectionIO[Option[User.Id]] =
    sql"select id from users where name = $name"
      .query[User.Id]
      .accumulate[Option]

  val indexQuery: doobie.ConnectionIO[List[User.Id]] =
    sql"select id from users".query[User.Id].accumulate[List]

  override def create(recipe: Recipe): F[User.Id] =
    for {
      hashedPassword <- passwordHasher.hashPassword(recipe.password.value)
      updatedRecipe = recipe.prepare(Password.Hashed(hashedPassword))
      id = UserStore.newId
      _ <- insertQuery(updatedRecipe, id).transact(transactor)
    } yield id

  override def delete(id: User.Id): F[Unit] =
    deleteQuery(id).void.transact(transactor)

  override def update(id: User.Id, recipe: Recipe): F[User] = {

    def updateWithHashedPassword(preparedRecipe: PreparedRecipe): F[User] = {
      for {
        _ <- updateQuery(id, preparedRecipe)
        p <- readQuery(id)
      } yield p
    }.transact(transactor)

    for {
      passwordHash <- passwordHasher.hashPassword(recipe.password.value)
      preparedRecipe = recipe.prepare(Password.Hashed(passwordHash))
      p <- updateWithHashedPassword(preparedRecipe)
    } yield p
  }

  override def read(id: User.Id): F[User] =
    readQuery(id).transact(transactor)


  override def index: F[List[User.Id]] =
    indexQuery.transact(transactor)

  override def findByName(name: String): F[Option[User]] =
    findIdByNameQuery(name)
      .flatMap(_.map(readQuery).sequence)
      .transact(transactor)

  override def grantPermission(
    userId: User.Id,
    component: Component,
    capability: Capability
  ): F[Unit] = {
    sql"insert into permissions (userid, component, capability) values ($userId, $component, $capability)"
      .update.run.transact(transactor).void
  }

  override def revokePermission(
    userId: User.Id,
    component: Component,
    capability: Capability
  ): F[Unit] = {
    sql"delete from permissions where userid = $userId and component = $component and capability = $capability"
      .update.run.transact(transactor).void
  }

}

