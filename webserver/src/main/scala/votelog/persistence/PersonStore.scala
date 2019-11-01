package votelog.persistence

import java.util.UUID

import votelog.domain.politics.Person
import votelog.infrastructure.ReadOnlyStoreAlg

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id]

