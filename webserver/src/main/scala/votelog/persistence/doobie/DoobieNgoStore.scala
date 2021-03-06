package votelog.persistence.doobie

import cats._
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.data.Sorting
import votelog.domain.data.Sorting.Direction
import votelog.domain.politics.Scoring.Score
import votelog.domain.politics.{Business, Ngo}
import votelog.persistence.NgoStore
import votelog.persistence.NgoStore.Recipe
import votelog.orphans.doobie.implicits._

class DoobieNgoStore[F[_]: NonEmptyParallel: ThrowableBracket](
  transactor: Transactor[F],
) extends NgoStore[F] {

  private def indexQuery(qp: IndexQueryParameters[(), Ngo.Field, Ngo.Field]) = {

    val toFieldName: Ngo.Field => String = {
      case Ngo.Field.Name => "name"
    }

    def toOrderPair(field: Ngo.Field, direction: Sorting.Direction) = {
      toFieldName(field) -> direction
    }

    val orderBy = buildOrderBy(qp.orderings.filter(o => qp.fields.contains(o._1)).map((toOrderPair _).tupled))
    val fields = Ngo.Field.values.map {
      field =>
        qp.fields.find( _ == field).map(toFieldName).getOrElse(s"null as ${toFieldName(field)}")
    }

    val selectFields = buildFields(fields)

    sql"select id $selectFields from ngos $orderBy"
      .query[(Ngo.Id, Ngo.Partial)].accumulate[List]
  }

  private def createQuery(recipe: Recipe, id: Ngo.Id) =
    sql"insert into ngos (id, name) values (${id.value}, ${recipe.name})"
      .update.run

  private def updateQuery(id: Ngo.Id, recipe: Recipe) =
    sql"update ngos set name = ${recipe.name} where id = $id".update.run

  private def deleteQuery(id: Ngo.Id) =
    sql"delete from ngos where id = $id".update.run

  private def readQuery(id: Ngo.Id) =
    sql"select name from ngos where id = $id".query[Ngo].unique

  private def upsertMotionScoreQuery(ngoId: Ngo.Id, motionId: Business.Id, score: Score) =
    for {
      scored <-
        sql"select count(*) > 0 from motions_scores where ngoid = $ngoId and motionid = $motionId"
          .query[Boolean]
          .unique
      _ <-
        if (scored)
          sql"update motions_scores set score = $score where ngoid = $ngoId and motionid = $motionId".update.run
        else
          sql"insert into motions_scores values ($ngoId, $motionId, $score)".update.run
    } yield ()

  private def deleteMotionScoreQuery(ngoId: Ngo.Id, motionId: Business.Id) =
    sql"delete from motions_scores where ngoid = $ngoId and motionid = $motionId".update.run


  private def selectMotionScore(ngoId: Ngo.Id): doobie.ConnectionIO[List[(Business.Id, Score)]] =
    sql"select motionid, score from motions_scores where ngoid = $ngoId"
      .query[(Business.Id, Score)]
      .accumulate[List]

  private val countQuery = sql"select count(id) from ngos".query[Int].unique


  override def index(params: IndexQueryParameters[(), Ngo.Field, Ngo.Field]): F[Index[Ngo.Id, Ngo.Partial]] = {
    val count = countQuery.transact(transactor)
    val entities = indexQuery(params).transact(transactor)
    (count, entities).parMapN(Index.apply)
  }


  override def create(r: Recipe): F[Ngo.Id] = {
    val id = NgoStore.newId
    createQuery(r, id)
      .transact(transactor)
      .map(_ => id)
  }

  override def delete(id: Ngo.Id): F[Unit] =
    deleteQuery(id)
      .transact(transactor)
      .void

  override def update(id: Ngo.Id, r: Recipe): F[Ngo] =
    updateQuery(id, r)
      .flatMap(_ => readQuery(id))
      .transact(transactor)

  override def read(n: ())(id: Ngo.Id): F[Ngo] =
    readQuery(id).transact(transactor)

  override def motionsScoredBy(ngo: Ngo.Id): F[List[(Business.Id, Score)]] =
    selectMotionScore(ngo).transact(transactor)

  override def scoreMotion(ngo: Ngo.Id, motion: Business.Id, score: Score): F[Unit] =
    upsertMotionScoreQuery(ngo, motion, score).transact(transactor)

  override def removeMotionScore(ngo: Ngo.Id, motion: Business.Id): F[Unit] =
    deleteMotionScoreQuery(ngo, motion).transact(transactor).void

}
