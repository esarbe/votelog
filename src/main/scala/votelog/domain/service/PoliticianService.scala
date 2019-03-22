package votelog.domain.service

import votelog.domain.model.Politician
import votelog.infrastructure.{CrudService, Identified}

trait PoliticianService[F[_]] extends CrudService[F, Politician]{
  override type Recipe = String
}
