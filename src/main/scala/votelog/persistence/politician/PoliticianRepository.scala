package votelog.persistence.politician

import votelog.domain.model.Politician
import votelog.infrastructure.CrudService

trait PoliticianRepository[F[_]] extends CrudService[F, Politician] {
  override type Recipe = String
}
