package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, Business}

trait BusinessStore[F[_]] extends ReadOnlyStoreAlg[F, Business, Business.Id, Business.Ordering] {
  type ReadParameters = Language
  type IndexParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context, Business.Ordering]
}
