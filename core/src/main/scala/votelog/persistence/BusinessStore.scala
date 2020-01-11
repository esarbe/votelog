package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Business}

trait BusinessStore[F[_]] extends ReadOnlyStoreAlg[F, Business, Business.Id] {
  type QueryParameters = Context
  type IndexQueryParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context]
}


