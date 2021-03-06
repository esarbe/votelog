package votelog.persistence

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Inside}
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.crudi.StoreAlg

trait StoreSpec extends AnyFlatSpec with Matchers with Inside with BeforeAndAfterAll {

  def aStore[Entity, Identity, Recipe, Partial, ReadParameters, IndexParameters](
    store: StoreAlg[IO, Entity, Identity, Recipe, Partial, ReadParameters, IndexParameters],
    creationRecipe: Recipe,
    createdEntity: Identity => Entity,
    updatedRecipe: Recipe,
    updatedEntity: Identity => Entity,
    partialEntity: Identity => Partial,
  )(
    queryParams: ReadParameters,
    indexQueryParams: IndexParameters,
  ): IO[Unit] = IO {

    it should "be able to store an entity" in {
      val check =
        for {
          indexBefore <- store.index(indexQueryParams)
          id <- store.create(creationRecipe)
          indexAfter <- store.index(indexQueryParams)
        } yield {
          indexBefore shouldBe Index(0, Nil)
          indexAfter shouldBe Index(1, List((id, partialEntity(id))))
        }

      check.unsafeRunSync()
    }

    it should "be able to read a stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case Index(_, List((id, partial))) =>
          val entity = store.read(queryParams)(id).unsafeRunSync()
          entity shouldBe createdEntity(id)
      }
    }

    it should "be able to update stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()
      inside(entities) {
        case Index(_, List((id, partial))) =>
          store.update(id, updatedRecipe).unsafeRunSync()
          val entity = store.read(queryParams)(id).unsafeRunSync()
          entity shouldBe updatedEntity(id)
      }
    }

    it should "be able to delete stored entity" in {
      val entities = store.index(indexQueryParams).unsafeRunSync()

      inside(entities) {
        case Index(_, List((id, partial))) =>
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
