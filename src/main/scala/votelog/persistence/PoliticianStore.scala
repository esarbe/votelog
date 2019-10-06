package votelog.persistence

import java.util.UUID

import votelog.domain.politics.Politician
import votelog.infrastructure.ReadOnlyStoreAlg

trait PoliticianStore[F[_]] extends ReadOnlyStoreAlg[F, Politician, Politician.Id]

