package votelog.persistence.doobie


import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.domain.politics.Politician
import votelog.persistence.PoliticianStore.Recipe
import votelog.persistence.{PoliticianStore, StoreSpec}

import scala.concurrent.ExecutionContext

class DoobiePoliticianStoreSpec
  extends FlatSpec
    with StoreSpec
    with ScalaFutures
    with Matchers
    with Inside {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val store = new DoobiePoliticianStore(transactor)
  val schema = new DoobieSchema(transactor)

  schema.initialize.unsafeRunSync()

  val creationRecipe: Recipe = PoliticianStore.Recipe("foo")
  val createdEntity: Politician.Id => Politician = Politician(_, "foo")
  val updatedRecipe: Recipe = Recipe("bar")
  val updatedEntity: Politician.Id => Politician = Politician(_, "bar")

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
}
