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
  val schema = new DoobieSchema(transactor)

  val motionStore = for {
    _ <- schema.initialize
    pid1 <- politician.create(PoliticianStore.Recipe("foo"))
    pid2 <- politician.create(PoliticianStore.Recipe("bar"))
    index <- politician.index
    _ = println(index)
    creationRecipe = MotionStore.Recipe("foo-motion", pid1)
    createdEntity = (id: Motion.Id) => Motion(id, "foo-motion", pid1)
    updatedRecipe = Recipe("updated-name", pid2)
    updatedEntity = (id: Motion.Id) => Motion(id, "updated-name", pid2)
    aMotionStore = aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
    mIndex <- store.index
    _ = println(mIndex)
  } yield aMotionStore

  it should behave like motionStore.unsafeRunSync()

}
