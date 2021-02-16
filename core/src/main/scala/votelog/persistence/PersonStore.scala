package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, Person, PersonPartial}

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[F, Person, Person.Id, PersonPartial, Person.Field, Person.Field] {
  type ReadParameters = Language
  type IndexParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context, Person.Field, Person.Field]
}

