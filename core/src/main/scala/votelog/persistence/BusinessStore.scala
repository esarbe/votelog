package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.politics.{Business, Context, Language}

trait BusinessStore[F[_]] extends ReadOnlyStoreAlg[
  F,
  Business,
  Business.Id,
  Business.Partial,
  Language,
  IndexQueryParameters[Context, Business.Field, Business.Field]] {

  type ReadParameters = Language
  type IndexParameters = IndexQueryParameters[Context, Business.Field, Business.Field]
}
