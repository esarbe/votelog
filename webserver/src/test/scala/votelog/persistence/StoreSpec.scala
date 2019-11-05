package votelog.persistence

import cats.effect.IO
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Inside, Matchers}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.crudi.StoreAlg

trait StoreSpec extends FlatSpec with Matchers with Inside with BeforeAndAfterAll {

  def aStore[Entity, Id, Recipe](
    store: StoreAlg[IO, Entity, Id, Recipe],
    creationRecipe: Recipe,
    createdEntity: Id => Entity,
    updatedRecipe: Recipe,
    updatedEntity: Id => Entity,
  ): IO[Unit] = IO {

    val queryParams = QueryParameters("en")
    val indexQueryParams =
      IndexQueryParameters(PageSize(0), Offset(0), queryParams)

    it should "be able to store an entity" in {
      val check =
        for {
          indexBefore <- store.index(indexQueryParams)
          id <- store.create(creationRecipe)
          indexAfter <- store.index(indexQueryParams)
        } yield {
          indexBefore shouldBe empty
          indexAfter.length shouldBe 1
          indexAfter shouldBe List(id)
        }

      check.unsafeRunSync()
    }

    it should "be able to read a stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case List(id) =>
          val entity = store.read(queryParams)(id).unsafeRunSync()
          entity shouldBe createdEntity(id)
      }
    }

    it should "be able to update stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()
      inside(entities) {
        case List(id) =>
          store.update(id, updatedRecipe).unsafeRunSync()

          val entity = store.read(queryParams)(id).unsafeRunSync()

          entity shouldBe updatedEntity(id)
      }
    }

    it should "be able to delete stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case List(id) =>
          val check =
            for {
              _ <- store.delete(id)
              entities <- store.index(indexQueryParams)
            } yield entities shouldBe empty

          check.unsafeRunSync()
      }
    }
  }
}
