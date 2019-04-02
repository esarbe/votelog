package votelog
package service

import votelog.domain.model.Politician
import votelog.persistence.politician.PoliticianRepository

class PoliticianServiceImpl[M[_]](val repository: PoliticianRepository[M])
  extends domain.service.PoliticianService[M] {
  type Identity = repository.Identity

  override def index: M[List[Identity]] = repository.index

  override def create(r: Recipe): M[Identity] =
    repository.create(PoliticianRepository.Recipe(r.name))

  override def delete(id: Identity): M[Unit] = repository.delete(id)

  override def update(id: Identity, t: Politician): M[Politician] = repository.update(id, t)

  override def read(id: Identity): M[Politician] = repository.read(id)
}