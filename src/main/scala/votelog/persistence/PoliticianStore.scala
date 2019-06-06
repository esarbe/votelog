package votelog.persistence

import java.util.UUID

import votelog.domain.politics.Politician
import votelog.infrastructure.StoreAlg

trait PoliticianStore[F[_]] extends StoreAlg[F, Politician, Politician.Id, PoliticianStore.Recipe]

object PoliticianStore {

  case class Recipe(name: String)

  def newId: Politician.Id = Politician.Id(UUID.randomUUID())
}
