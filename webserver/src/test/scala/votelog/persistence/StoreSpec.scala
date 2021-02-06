package votelog.persistence

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Inside}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters, QueryParameters}
import votelog.domain.crudi.StoreAlg
import votelog.domain.politics.Language

trait StoreSpec extends AnyFlatSpec with Matchers with Inside with BeforeAndAfterAll {

  def aStore[Entity, Id, Recipe, Ordering](
    store: StoreAlg[IO, Entity, Id, Recipe, Ordering],
    creationRecipe: Recipe,
    createdEntity: Id => Entity,
    updatedRecipe: Recipe,
    updatedEntity: Id => Entity)(
    queryParams: store.ReadParameters,
    indexQueryParams: store.IndexParameters,
  ): IO[Unit] = IO {

    it should "be able to store an entity" in {
      val check =
        for {
          indexBefore <- store.index(indexQueryParams)
          id <- store.create(creationRecipe)
          indexAfter <- store.index(indexQueryParams)
        } yield {
          indexBefore shouldBe Index(0, Nil)
          indexAfter shouldBe Index(1, List(id))
        }

      check.unsafeRunSync()
    }

    it should "be able to read a stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case Index(_, List(id)) =>
          val entity = store.read(queryParams)(id).unsafeRunSync()
          entity shouldBe createdEntity(id)
      }
    }

    it should "be able to update stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()
      inside(entities) {
        case Index(_, List(id)) =>
          store.update(id, updatedRecipe).unsafeRunSync()
          val entity = store.read(queryParams)(id).unsafeRunSync()
          entity shouldBe updatedEntity(id)
      }
    }

    it should "be able to delete stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case Index(_, List(id)) =>
          val check =
            for {
              _ <- store.delete(id)
              index <- store.index(indexQueryParams)
            } yield index shouldBe Index(0, Nil)

          check.unsafeRunSync()
      }
    }
  }
}
