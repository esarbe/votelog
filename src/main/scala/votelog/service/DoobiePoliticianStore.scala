package votelog.service

import cats.Monad
import votelog.domain.model.Politician
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.persistence.PoliticianStore
import votelog.persistence.PoliticianStore.Recipe


abstract class DoobiePoliticianStore[F[_]: Monad] extends PoliticianStore[F] {

  val transactor: doobie.util.transactor.Transactor[F]

  def readQuery(id: Politician.Id): ConnectionIO[Politician]
  = sql"select id, name from politician where id=${id}".query[Politician].unique

  def deleteQuery(id: Politician.Id): doobie.ConnectionIO[Int] =
    sql"delete from politician where id = ${id}"
      .update.run

  def updateQuery(id: Politician.Id, entity: Politician) =
    sql"update politician set name = ${entity.name} where id = ${entity.id}"

  def insertQuery(recipe: Recipe): doobie.ConnectionIO[Politician.Id] =
    sql"insert into politician (name) values (${recipe.name})"
      .update
      .withUniqueGeneratedKeys[Politician.Id]("id")

  val indexQuery: doobie.ConnectionIO[List[Politician.Id]] =
    sql"select id from politician".query[Politician.Id].accumulate[List]

  override def create(recipe: Recipe): F[Politician.Id] =
    insertQuery(recipe).transact(transactor)

  override def delete(id: Politician.Id): F[Unit] =
    deleteQuery(id).map(_ => ()).transact(transactor)

  override def update(id: Politician.Id, t: Politician): F[Politician] = {
    for {
      _ <- updateQuery(id,t).update.run
      p <- readQuery(t.id)
    } yield p
  }.transact(transactor)

  override def read(id: Politician.Id): F[Politician] =
    readQuery(id).transact(transactor)


  override def index: F[List[Politician.Id]] =
    indexQuery.transact(transactor)
}
