package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Business, Context, Language}

trait BusinessStore[F[_]] extends ReadOnlyStoreAlg[F, Business, Business.Id] {
  type QueryParameters = Language
  type IndexQueryParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context]
}
