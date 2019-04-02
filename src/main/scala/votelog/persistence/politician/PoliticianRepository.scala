package votelog.persistence.politician

import votelog.domain.model.Politician
import votelog.infrastructure.CrudService

trait PoliticianRepository[F[_]] extends CrudService[F, Politician] {
  type Recipe = PoliticianRepository.Recipe
  type Identity = Politician.Id
}

object PoliticianRepository {
  case class Recipe(name: String)
}
