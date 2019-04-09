package votelog.persistence

import votelog.domain.model.Politician
import votelog.infrastructure.StoreAlg

trait PoliticianStore[F[_]] extends StoreAlg[F, Politician, Politician.Id, PoliticianStore.Recipe]

object PoliticianStore {
  case class Recipe(name: String)
}
