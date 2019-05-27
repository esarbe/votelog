package votelog.persistence.doobie


import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.domain.politics.Motion
import votelog.persistence.MotionStore.Recipe
import votelog.persistence.{MotionStore, PoliticianStore, StoreSpec}
import cats.implicits._
import scala.concurrent.ExecutionContext

class DoobieMotionStoreSpec
  extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  val store = new DoobieMotionStore(transactor)
  val politician = new DoobiePoliticianStore(transactor)

  val pid1 = PoliticianStore.newId
  val pid2 = PoliticianStore.newId

  val creationRecipe: Recipe = MotionStore.Recipe(MotionStore.newId, "foo-motion", pid1)
  val createdEntity: Motion.Id => Motion = Motion(_, "foo-motion", pid1)
  val updatedRecipe: Recipe = Recipe(MotionStore.newId, "updated-name", pid2)
  val updatedEntity: Motion.Id => Motion = Motion(_, "updated-name", pid2)

  val setup: IO[Unit] = IO {
    politician.create(PoliticianStore.Recipe(pid1, "foo")) *>
      politician.create(PoliticianStore.Recipe(pid2, "bar"))
  }

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity, setup)
}
