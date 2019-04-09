package votelog.service

import cats.Monad
import cats.effect.IO
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.model.Motion
import votelog.persistence.MotionStore
import votelog.persistence.MotionStore.Recipe


abstract class DoobieMotionStore[F[_]: Monad] extends MotionStore[F] {

  val transactor: doobie.util.transactor.Transactor[F]

  def readQuery(id: Motion.Id): ConnectionIO[Motion]
    = sql"select id, name, submitter from motion where id=${id}".query[Motion].unique

  def deleteQuery(id: Motion.Id): doobie.ConnectionIO[Int] =
    sql"delete from motion where id = ${id}"
      .update.run

  def updateQuery(id: Motion.Id, entity: Motion) =
    sql"update motion set name = ${entity.name}, submitter = ${entity.submitter} where id = ${entity.id}"

  def insertQuery(recipe: Recipe): doobie.ConnectionIO[Motion.Id] =
    sql"insert into motion (name, submitter) values (${recipe.name}, ${recipe.submitter})"
      .update
      .withUniqueGeneratedKeys[Motion.Id]("id")

  val indexQuery: doobie.ConnectionIO[List[Motion.Id]] =
    sql"select id from motion".query[Motion.Id].accumulate[List]

  override def create(recipe: Recipe): F[Motion.Id] =
    insertQuery(recipe).transact(transactor)

  override def delete(id: Motion.Id): F[Unit] =
    deleteQuery(id).map(_ => ()).transact(transactor)

  override def update(id: Motion.Id, t: Motion): F[Motion] = {
    for {
      _ <- updateQuery(id,t).update.run
      p <- readQuery(t.id)
    } yield p
  }.transact(transactor)

  override def read(id: Motion.Id): F[Motion] =
    readQuery(id).transact(transactor)

  override def index: F[List[Motion.Id]] =
    indexQuery.transact(transactor)
}
