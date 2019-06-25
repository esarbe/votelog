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
class DoobieMotionStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends MotionStore[F] {

  private def selectQuery(id: Motion.Id): ConnectionIO[Motion] =
    sql"select name, submitter from motions where id=${id}".query[Motion].unique

  private def deleteQuery(id: Motion.Id): doobie.ConnectionIO[Int] =
    sql"delete from motions where id = ${id}"
      .update.run

  private def updateQuery(id: Motion.Id, recipe: Recipe) =
    sql"update motions set name = ${recipe.name}, submitter = ${recipe.submitter} where id = $id"
      .update.run

  private def insertQuery(recipe: Recipe, id: Motion.Id) =
    sql"insert into motions (id, name, submitter) values (${id}, ${recipe.name}, ${recipe.submitter.value})"
      .update
      .run

  val indexQuery: doobie.ConnectionIO[List[Motion.Id]] =
    sql"select id from motions".query[Motion.Id].accumulate[List]

  override def create(recipe: Recipe): F[Motion.Id] = {
    val id = MotionStore.newId

    insertQuery(recipe, id).transact(transactor) *> Monad[F].pure(id)
  }

  override def delete(id: Motion.Id): F[Unit] =
    deleteQuery(id).void.transact(transactor)

  override def update(id: Motion.Id, r: Recipe): F[Motion] = {
    for {
      _ <- updateQuery(id, r)
      p <- selectQuery(id)
    } yield p
  }.transact(transactor)

  override def read(id: Motion.Id): F[Motion] =
    selectQuery(id).transact(transactor)

  override def index: F[List[Motion.Id]] =
    indexQuery.transact(transactor)

}
