package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Person}

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id] {
  type QueryParameters = Context
  type IndexQueryParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context]
}

