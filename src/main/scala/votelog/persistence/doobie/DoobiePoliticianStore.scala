package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.log.LogHandler
import votelog.domain.politics.Politician
import votelog.persistence.PoliticianStore
import votelog.persistence.PoliticianStore.Recipe
import votelog.persistence.doobie.Mappings._

class DoobiePoliticianStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends PoliticianStore[F] {

  def selectQuery(id: Politician.Id): ConnectionIO[Politician] =
    sql"select name from politicians where id=${id}"
      .query[Politician]
      .unique

  def deleteQuery(id: Politician.Id): doobie.ConnectionIO[Int] =
    sql"delete from politicians where id = ${id}"
      .update.run

  def updateQuery(id: Politician.Id, recipe: Recipe) =
    sql"update politicians set name = ${recipe.name} where id = $id"

  def insertQuery(recipe: Recipe, id: Politician.Id) =
    sql"insert into politicians (id, name) values ($id, ${recipe.name})"
      .update
      .run

  val indexQuery: doobie.ConnectionIO[List[Politician.Id]] =
    sql"select id from politicians".query[Politician.Id].accumulate[List]

  override def create(recipe: Recipe): F[Politician.Id] = {
    val id = PoliticianStore.newId
    insertQuery(recipe, id).transact(transactor) *> Monad[F].pure(id)
  }

  override def delete(id: Politician.Id): F[Unit] =
    deleteQuery(id).void.transact(transactor)

  override def update(id: Politician.Id, r: Recipe): F[Politician] = {
    for {
      _ <- updateQuery(id, r).update.run
      p <- selectQuery(id)
    } yield p
  }.transact(transactor)

  override def read(id: Politician.Id): F[Politician] =
    selectQuery(id).transact(transactor)


  override def index: F[List[Politician.Id]] =
    indexQuery.transact(transactor)
}
