package votelog.persistence.doobie


import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Inside, Matchers, WordSpec}
import votelog.persistence.MotionStore.Recipe
import votelog.persistence.{MotionStore, PoliticianStore}

import scala.concurrent.ExecutionContext

class DoobieMotionStoreSpec extends WordSpec with ScalaFutures with Matchers with Inside {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor =
    Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "sa",
      "",
    )

  val store = new DoobieMotionStore(transactor)
  val politician = new DoobiePoliticianStore(transactor)
  val schema = new DoobieSchema(transactor)

  schema.initialize.unsafeRunSync()

  "DoobieMotionStore" should {
    val pid1 = politician.create(PoliticianStore.Recipe("foo")).unsafeRunSync()
    val pid2 = politician.create(PoliticianStore.Recipe("bar")).unsafeRunSync()

    val recipe = MotionStore.Recipe("foo-motion", pid1)

    "be able to store an entity" in {
      val check =
        for {
          indexBefore <- store.index
          _ <- store.create(recipe)
          indexAfter <- store.index
        } yield {
          indexBefore shouldBe empty
          indexAfter.length shouldBe 1
        }

      check.unsafeRunSync()
    }


    "be able to read a stored entity" in {
      val entities = store.index.unsafeRunSync()

      inside(entities) { case List(id) =>
        val entity = store.read(id).unsafeRunSync()
        entity.submitter shouldBe recipe.submitter
        entity.name shouldBe recipe.name
      }
    }

    "be able to update stored entity" in {
      val entities = store.index.unsafeRunSync()
      inside(entities) { case List(id) =>
        store.update(id, Recipe("updated-name", pid2)).unsafeRunSync()

        val updatedEntity = store.read(id).unsafeRunSync()

        updatedEntity.name shouldBe "updated-name"
        updatedEntity.submitter shouldBe pid2
      }
    }


    "be able to delete stored entity" in {
      val entities = store.index.unsafeRunSync()

      inside(entities) { case List(id) =>
        val check =
          for {
            _ <- store.delete(id)
            entities <- store.index
          } yield entities shouldBe empty

        check.unsafeRunSync()
      }
    }
  }
}
