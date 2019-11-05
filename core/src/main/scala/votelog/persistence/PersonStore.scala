package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.Person

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id]

