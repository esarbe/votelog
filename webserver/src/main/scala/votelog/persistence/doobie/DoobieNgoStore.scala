package votelog.persistence.doobie

import cats._
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.domain.politics.Scoring.{Score, Weight}
import votelog.domain.politics.{Motion, Ngo}
import votelog.infrastructure.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.persistence.NgoStore
import votelog.persistence.NgoStore.Recipe
import votelog.persistence.doobie.Mappings._

class DoobieNgoStore[F[_]: Monad: ThrowableBracket](
  transactor: Transactor[F],
) extends NgoStore[F] {

  private val indexQuery =
    sql"select id from ngos".query[Ngo.Id].accumulate[List]

  private def createQuery(recipe: Recipe, id: Ngo.Id) =
    sql"insert into ngos (id, name) values ($id, ${recipe.name})"
      .update.run

  private def updateQuery(id: Ngo.Id, recipe: Recipe) =
    sql"update ngos set name = ${recipe.name} where id = $id".update.run

  private def deleteQuery(id: Ngo.Id) =
    sql"delete from ngos where id = $id".update.run

  private def readQuery(id: Ngo.Id) =
    sql"select name from ngos where id = $id".query[Ngo].unique

  private def upsertMotionScoreQuery(ngoId: Ngo.Id, motionId: Motion.Id, score: Score) =
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

  private def deleteMotionScoreQuery(ngoId: Ngo.Id, motionId: Motion.Id) =
    sql"delete from motions_scores where ngoid = $ngoId and motionid = $motionId".update.run


  private def selectMotionScore(ngoId: Ngo.Id): doobie.ConnectionIO[List[(Motion.Id, Score)]] =
    sql"select motionid, score from motions_scores where ngoid = $ngoId"
      .query[(Motion.Id, Score)]
      .accumulate[List]


  override def index(params: IndexQueryParameters): F[List[Ngo.Id]] =
    indexQuery.transact(transactor)


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

  override def read(p: QueryParameters)(id: Ngo.Id): F[Ngo] =
    readQuery(id).transact(transactor)

  override def motionsScoredBy(ngo: Ngo.Id): F[List[(Motion.Id, Score)]] =
    selectMotionScore(ngo).transact(transactor)

  override def scoreMotion(ngo: Ngo.Id, motion: Motion.Id, score: Score): F[Unit] =
    upsertMotionScoreQuery(ngo, motion, score).transact(transactor)

  override def removeMotionScore(ngo: Ngo.Id, motion: Motion.Id): F[Unit] =
    deleteMotionScoreQuery(ngo, motion).transact(transactor).void

}