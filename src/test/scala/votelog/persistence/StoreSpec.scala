package votelog.persistence

import cats.effect.IO
import _root_.doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers, WordSpec}
import votelog.infrastructure.StoreAlg
import votelog.persistence.doobie.DoobieSchema
import cats.effect.syntax._
import scala.concurrent.ExecutionContext
import cats.implicits._

trait StoreSpec extends FlatSpec with Matchers with Inside with BeforeAndAfterAll {
  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor =
    Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql:postgres",
        "postgres",
        "raclette"
    )

  def aStore[Entity, Id, Recipe](
    store: StoreAlg[IO, Entity, Id, Recipe],
    creationRecipe: Recipe,
    createdEntity: Id => Entity,
    updatedRecipe: Recipe,
    updatedEntity: Id => Entity,
  ) {

    (new DoobieSchema(transactor).initialize).unsafeRunSync()

    it should "be able to store an entity" in {
      val check =
        for {
          indexBefore <- store.index
          _ <- store.create(creationRecipe)
          indexAfter <- store.index
        } yield {
          indexBefore shouldBe empty
          indexAfter.length shouldBe 1
        }

      check.unsafeRunSync()
    }

    it should "be able to read a stored entity" in {
      val entities = store.index.unsafeRunSync()

      inside(entities) {
        case List(id) =>
          val entity = store.read(id).unsafeRunSync()

          import _root_.doobie.implicits._

          sql"select * from motions".query

          println(entity)

          entity shouldBe createdEntity(id)
      }
    }

    it should "be able to update stored entity" in {
      val entities = store.index.unsafeRunSync()
      inside(entities) {
        case List(id) =>
          store.update(id, updatedRecipe).unsafeRunSync()

          val entity = store.read(id).unsafeRunSync()

          entity shouldBe updatedEntity(id)
      }
    }

    ignore should "be able to delete stored entity" in {
      val entities = store.index.unsafeRunSync()

      inside(entities) {
        case List(id) =>
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
