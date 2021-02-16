package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Business, Context, Language}

trait BusinessStore[F[_]] extends ReadOnlyStoreAlg[F, Business, Business.Id, Business.Partial, Business.Field, Business.Field] {
  type ReadParameters = Language
  type IndexParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context, Business.Field, Business.Field]
}
