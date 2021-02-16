package votelog.persistence.doobie

import cats.Id
import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import votelog.domain.politics.{Business, Ngo}
import votelog.domain.politics.Scoring.Score
import votelog.persistence.NgoStore.Recipe
import votelog.persistence.{NgoStore, StoreSpec}

import scala.concurrent.ExecutionContext

class DoobieNgoStoreSpec
  extends AnyFlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val schema = new DoobieSchema(transactor)
  val store = new DoobieNgoStore(transactor)

  schema.initialize.unsafeRunSync()

  val creationRecipe: Recipe = NgoStore.Recipe("Die Iliberalen")
  val createdEntity: Ngo.Id => Ngo = _ => Ngo("Die Iliberalen")
  val updatedRecipe: Recipe = Recipe("Pink Panther")
  val updatedEntity: Ngo.Id => Ngo = _ => Ngo("Pink Panther")

  val ngoStore =
    for {
      ngoStore <- aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity, (_: Any) => Ngo.empty)((), ())
    } yield ngoStore

  it should behave like ngoStore.unsafeRunSync()


  it should "update an existing record" in {

    val check =
      for {
        nid <- store.create(NgoStore.Recipe("Earthicans"))
        before <- store.motionsScoredBy(nid)
        mid = Business.Id(1)
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
        nid <- store.create(NgoStore.Recipe("Earthicans"))
        before <- store.motionsScoredBy(nid)
        mid = Business.Id(1)
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
