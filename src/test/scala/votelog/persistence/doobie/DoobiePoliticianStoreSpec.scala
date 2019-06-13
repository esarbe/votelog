package votelog.persistence.doobie

import cats.effect.{ContextShift, IO}
import cats.implicits._
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

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val schema = new DoobieSchema(transactor)
  val store = new DoobiePoliticianStore(transactor)

  val creationRecipe: Recipe = PoliticianStore.Recipe("Francois Fondue")
  val createdEntity: Politician.Id => Politician = _ => Politician("Francois Fondue")
  val updatedRecipe: Recipe = Recipe("Herman Rösti")
  val updatedEntity: Politician.Id => Politician = _ => Politician("Herman Rösti")

  val politicianStore =
    for {
      _ <- schema.initialize
      index <- store.index.map(_.map(store.read)).flatMap(_.sequence)
      politicianStore <- aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
    } yield politicianStore

  it should behave like politicianStore.unsafeRunSync()
}
