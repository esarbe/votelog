package votelog.persistence.doobie

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


  def readQuery(id: User.Id): ConnectionIO[User] = {
    val selectUser =
      sql"select name, email, passwordhash from user where id=$id"
        .query[(String, String, String)]
        .unique

    val selectPermissions =
      sql"select capability, component from permission where userid = $id"
        .query[User.Permission]
        .accumulate[List]

    for {
      (name, email, hashedPassword) <- selectUser
      permissions <- selectPermissions
    } yield User(name, User.Email(email), hashedPassword, permissions.toSet)
  }


  def deleteQuery(id: User.Id): doobie.ConnectionIO[Int] =
    sql"delete from user where id = ${id}"
      .update.run

  def updateQuery(id: User.Id, recipe: PreparedRecipe) =
    sql"""
         |update user set
         |name = ${recipe.name},
         |email = ${recipe.email},
         |passwordhash = ${recipe.password}
         |where id = $id
         |"""
      .stripMargin.update.run

  def insertQuery(recipe: PreparedRecipe): doobie.ConnectionIO[User.Id] =
    sql"""
        insert into user (name, email, passwordhash)
          values (${recipe.name}, ${recipe.email}, ${recipe.password})
    """
      .update
      .withUniqueGeneratedKeys[User.Id]("id")

  def findIdByNameQuery(name: String): doobie.ConnectionIO[Option[User.Id]] =
    sql"select id from user where name = $name"
      .query[User.Id]
      .accumulate[Option]

  val indexQuery: doobie.ConnectionIO[List[User.Id]] =
    sql"select id from user".query[User.Id].accumulate[List]

  override def create(recipe: Recipe): F[User.Id] = {

    val createUser =
      for {
        hashedPassword <- passwordHasher.hashPassword(recipe.password.value)
        updatedRecipe = recipe.prepare(Password.Hashed(hashedPassword)) // this is typesafe
        id <- insertQuery(updatedRecipe).transact(transactor)
      } yield id


    val readOrCreate = // there must be a better way ...
      findIdByNameQuery(recipe.name)
        .map { maybeUser =>
           maybeUser.map(userid => Monad[F].pure(userid)).getOrElse(createUser)
        }

    readOrCreate.transact(transactor).flatten
  }

  override def delete(id: User.Id): F[Unit] =
    deleteQuery(id).map(_ => ()).transact(transactor)

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
    sql"insert into permission (userid, component, capability) values ($userId, $component, $capability)"
      .update.run.transact(transactor).map(_ => ())
  }

  override def revokePermission(
    userId: User.Id,
    component: Component,
    capability: Capability
  ): F[Unit] = {
    sql"delete from permission where userid = $userId and component = $component and capability = $capability"
      .update.run.transact(transactor).map(_ => ())
  }
}

