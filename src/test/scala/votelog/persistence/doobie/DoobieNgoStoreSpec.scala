package votelog.persistence.doobie

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Inside, Matchers}
import org.scalatest.concurrent.ScalaFutures
import votelog.domain.politics.Ngo
import votelog.domain.politics.Scoring.Score
import votelog.persistence.NgoStore.Recipe
import votelog.persistence.{MotionStore, NgoStore, PoliticianStore, StoreSpec}

import scala.concurrent.ExecutionContext

class DoobieNgoStoreSpec
  extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val schema = new DoobieSchema(transactor)
  val store = new DoobieNgoStore(transactor)
  val politicians = new DoobiePoliticianStore(transactor)
  val motions = new DoobieMotionStore(transactor)

  val creationRecipe: Recipe = NgoStore.Recipe("Die Iliberalen")
  val createdEntity: Ngo.Id => Ngo = _ => Ngo("Die Iliberalen")
  val updatedRecipe: Recipe = Recipe("Pink Panther")
  val updatedEntity: Ngo.Id => Ngo = _ => Ngo("Pink Panther")

  override def beforeAll(): Unit = {
    schema.initialize.unsafeRunSync()
  }

  val ngoStore =
    for {
      ngoStore <- aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
    } yield ngoStore

  it should behave like ngoStore.unsafeRunSync()


  it should "update an existing record" in {

    val check =
      for {
        pid <- politicians.create(PoliticianStore.Recipe("Freddy"))
        mid <- motions.create(MotionStore.Recipe("Move To Mars", pid))
        nid <- store.create(NgoStore.Recipe("Earthicans"))

        before <- store.motionsScoredBy(nid)
        _ <- store.scoreMotion(nid, mid, Score(0.0))
        afterScoring <- store.motionsScoredBy(nid)
        _ <- store.scoreMotion(nid, mid, Score(0.5))
        afterUpdate <- store.motionsScoredBy(nid)
      } yield {
        before shouldBe Nil
        afterScoring shouldBe List(mid -> Score(0.0))
        afterUpdate shouldBe List(mid -> Score(0.5))
      }

    check.unsafeRunSync()
  }

  it should "remove an existing record" in {
    val check =
      for {
        pid <- politicians.create(PoliticianStore.Recipe("Freddy"))
        mid <- motions.create(MotionStore.Recipe("Move To Mars", pid))
        nid <- store.create(NgoStore.Recipe("Earthicans"))
        before <- store.motionsScoredBy(nid)
        _ <- store.scoreMotion(nid, mid, Score(0.0))
        afterScoring <- store.motionsScoredBy(nid)
        _ <- store.removeMotionScore(nid, mid)
        afterUpdate <- store.motionsScoredBy(nid)
      } yield {
        before shouldBe Nil
        afterScoring shouldBe List(mid -> Score(0.0))
        afterUpdate shouldBe Nil
      }

    check.unsafeRunSync()
  }

}
