package votelog.persistence.doobie

import cats._
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.domain.politics.Ngo
import votelog.persistence.NgoStore
import votelog.persistence.NgoStore.Recipe


class DoobieNgoStore[F[_]: Monad](
  transactor: Transactor[F],
) extends NgoStore[F] {

  private val indexQuery =
    sql"select id from ngo".query[Ngo.Id].accumulate[List]

  private def createQuery(recipe: Recipe): doobie.ConnectionIO[Ngo.Id] =
    sql"insert into ngo (name) values (${recipe.name})"
      .update
      .withUniqueGeneratedKeys[Ngo.Id]("id")

  private def updateQuery(id: Ngo.Id, recipe: Recipe) =
    sql"update ngo set name = ${recipe.name} where id = $id".update.run

  private def deleteQuery(id: Ngo.Id) =
    sql"delete from ngo where id = $id".update.run

  private def readQuery(id: Ngo.Id) =
    sql"select * from ngo where id = $id".query[Ngo].unique

  override def index: F[List[Ngo.Id]] =
    indexQuery.transact(transactor)

  override def create(r: Recipe): F[Ngo.Id] =
    createQuery(r)
      .transact(transactor)

  override def delete(id: Ngo.Id): F[Unit] =
    deleteQuery(id)
      .transact(transactor)
      .map( _ => ())

  override def update(id: Ngo.Id, r: Recipe): F[Ngo] =
    updateQuery(id, r)
      .flatMap(_ => readQuery(id))
      .transact(transactor)

  override def read(id: Ngo.Id): F[Ngo] =
    readQuery(id).transact(transactor)
}
