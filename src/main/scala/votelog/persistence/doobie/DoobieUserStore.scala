package votelog.persistence.doobie

import cats._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.{Meta, Read}
import votelog.domain.authorization.Capability.Create
import votelog.domain.authorization.{Capability, Component, User}
import votelog.domain.authorization.User.Permission
import votelog.persistence.UserStore
import votelog.persistence.UserStore.Recipe


class DoobieUserStore[F[_]: Monad](
  transactor: doobie.util.transactor.Transactor[F]
) extends UserStore[F] {

  val q = implicitly[Read[Component]]
  implicit val metaCapability =
    Meta[String]
      .imap {
        case "Read" => Capability.Read
        case "Create" => Capability.Create
        case "Update" => Capability.Update
        case "Delete" => Capability.Delete
      }(Capability.showComponent.show)

  //implicit def permissionMeta = implicitly[Read[Permission]]


  def readQuery(id: User.Id): ConnectionIO[User] =
    for {
      (name, email) <- sql"select name, email from user where id=${id}".query[(String, String)].unique
    } yield User(name, User.Email(email), Set.empty)

  def deleteQuery(id: User.Id): doobie.ConnectionIO[Int] =
    sql"delete from user where id = ${id}"
      .update.run

  def updateQuery(id: User.Id, recipe: Recipe) =
    sql"update user set name = ${recipe.name} where id = $id"

  def insertQuery(recipe: Recipe): doobie.ConnectionIO[User.Id] =
    sql"insert into user (name) values (${recipe.name})"
      .update
      .withUniqueGeneratedKeys[User.Id]("id")

  val indexQuery: doobie.ConnectionIO[List[User.Id]] =
    sql"select id from user".query[User.Id].accumulate[List]

  override def create(recipe: Recipe): F[User.Id] =
    insertQuery(recipe).transact(transactor)

  override def delete(id: User.Id): F[Unit] =
    deleteQuery(id).map(_ => ()).transact(transactor)

  override def update(id: User.Id, r: Recipe): F[User] = {
    for {
      _ <- updateQuery(id, r).update.run
      p <- readQuery(id)
    } yield p
  }.transact(transactor)

  override def read(id: User.Id): F[User] =
    readQuery(id).transact(transactor)


  override def index: F[List[User.Id]] =
    indexQuery.transact(transactor)
}

