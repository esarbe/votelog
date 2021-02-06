package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, Person}

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id, Person.Ordering] {
  type ReadParameters = Language
  type IndexParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context, Person.Ordering]
}

