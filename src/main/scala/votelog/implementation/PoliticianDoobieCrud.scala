package votelog.implementation

import votelog.domain.model.Politician
import votelog.domain.service.PoliticianService
import votelog.infrastructure.Identified
import votelog.persistence.politician.PoliticianRepository

class PoliticianDoobieCrud[F[_]] extends PoliticianService[F] with PoliticianRepository[F]{
  override implicit val I: Identified[Politician] = IdentifiedPolitician

  override def create(r: String): F[Politician] = ???

  override def delete(id: I.Identity): F[Politician] = ???

  override def update(t: Politician): F[Politician] = ???

  override def read(id: I.Identity): F[Politician] = ???
}
