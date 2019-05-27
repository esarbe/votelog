package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.politics.Motion
import votelog.persistence.MotionStore
import votelog.persistence.MotionStore.Recipe
import votelog.persistence.doobie.Mappings._
import doobie.postgres.implicits._
class DoobieMotionStore[F[_]: Monad](
  transactor: doobie.util.transactor.Transactor[F]
) extends MotionStore[F] {

  def readQuery(id: Motion.Id): ConnectionIO[Motion] =
    sql"select id, name, submitter from motions where id=${id}".query[Motion].unique

  def deleteQuery(id: Motion.Id): doobie.ConnectionIO[Int] =
    sql"delete from motions where id = ${id}"
      .update.run

  def updateQuery(id: Motion.Id, recipe: Recipe) =
    sql"update motions set name = ${recipe.name}, submitter = ${recipe.submitter} where id = $id"

  def insertQuery(recipe: Recipe): doobie.ConnectionIO[Motion.Id] =
    sql"insert into motions (id, name, submitter) values (${recipe.id}, ${recipe.name}, ${recipe.submitter.value})"
      .update
      .withUniqueGeneratedKeys[Motion.Id]("id")

  val indexQuery: doobie.ConnectionIO[List[Motion.Id]] =
    sql"select id from motions".query[Motion.Id].accumulate[List]

  override def create(recipe: Recipe): F[Motion.Id] =
    insertQuery(recipe).transact(transactor)

  override def delete(id: Motion.Id): F[Unit] =
    deleteQuery(id).map(_ => ()).transact(transactor)

  override def update(id: Motion.Id, r: Recipe): F[Motion] = {
    for {
      _ <- updateQuery(id, r).update.run
      p <- readQuery(id)
    } yield p
  }.transact(transactor)

  override def read(id: Motion.Id): F[Motion] =
    readQuery(id).transact(transactor)

  override def index: F[List[Motion.Id]] =
    indexQuery.transact(transactor)
}
