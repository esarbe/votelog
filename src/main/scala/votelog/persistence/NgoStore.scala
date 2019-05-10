package votelog.persistence

import votelog.domain.politics.Ngo
import votelog.infrastructure.StoreAlg
import votelog.persistence.NgoStore.Recipe

trait NgoStore[F[_]] extends StoreAlg[F, Ngo, Ngo.Id, Recipe]

object NgoStore {
  case class Recipe(name: String)
}