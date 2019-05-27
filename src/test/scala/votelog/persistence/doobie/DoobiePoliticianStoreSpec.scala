package votelog.persistence.doobie


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

  val store = new DoobiePoliticianStore(transactor)

  val creationRecipe: Recipe = PoliticianStore.Recipe(PoliticianStore.newId, "Francois Fondue")
  val createdEntity: Politician.Id => Politician = Politician(_, "Francois Fondue")
  val updatedRecipe: Recipe = Recipe(PoliticianStore.newId, "Herman Rösti")
  val updatedEntity: Politician.Id => Politician = Politician(_, "Herman Rösti")

  it should behave like aStore(store, creationRecipe, createdEntity, updatedRecipe, updatedEntity)
}
