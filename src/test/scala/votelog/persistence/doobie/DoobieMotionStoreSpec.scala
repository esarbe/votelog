package votelog.persistence.doobie


import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.domain.politics.Motion
import votelog.persistence.MotionStore.Recipe
import votelog.persistence.{MotionStore, PoliticianStore, StoreSpec}

import scala.concurrent.ExecutionContext

class DoobieMotionStoreSpec
  extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val store = new DoobieMotionStore(transactor)
  val politician = new DoobiePoliticianStore(transactor)
  val schema = new DoobieSchema(transactor)

  schema.initialize.unsafeRunSync()

  val pid1 = politician.create(PoliticianStore.Recipe("foo")).unsafeRunSync()
  val pid2 = politician.create(PoliticianStore.Recipe("bar")).unsafeRunSync()

  val creationRecipe: Recipe = MotionStore.Recipe("foo-motion", pid1)
  val createdEntity: Motion.Id => Motion = Motion(_, "foo-motion", pid1)
  val updatedRecipe: Recipe = Recipe("updated-name", pid2)
  val updatedEntity: Motion.Id => Motion = Motion(_, "updated-name", pid2)

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
}
