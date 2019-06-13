package votelog.persistence.doobie


import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.domain.politics.{Motion, Politician}
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

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val store = new DoobieMotionStore(transactor)
  val politician = new DoobiePoliticianStore(transactor)
  val schema = new DoobieSchema(transactor)

  val motionStore = for {
    _ <- schema.initialize
    pid1 <- politician.create(PoliticianStore.Recipe("foo"))
    pid2 <- politician.create(PoliticianStore.Recipe("bar"))
    creationRecipe = MotionStore.Recipe("foo-motion", pid1)
    createdEntity = (_: Motion.Id) => Motion("foo-motion", pid1)
    updatedRecipe = Recipe("updated-name", pid2)
    updatedEntity = (_: Motion.Id) => Motion("updated-name", pid2)
    aMotionStore <- aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
  } yield aMotionStore

  it should behave like motionStore.unsafeRunSync()

}
