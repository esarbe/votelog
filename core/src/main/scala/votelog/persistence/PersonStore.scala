package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.politics.{Context, Language, Person, PersonPartial}

trait PersonStore[F[_]] extends ReadOnlyStoreAlg[
  F,
  Person,
  Person.Id,
  PersonPartial,
  Language,
  IndexQueryParameters[Context, Person.Field, Person.Field]] {

  type IndexParameters = IndexQueryParameters[Context, Person.Field, Person.Field]

}

