package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, Person}

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id] {
  type QueryParameters = Language
  type IndexQueryParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context]
}

