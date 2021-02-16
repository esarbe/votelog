package votelog.endpoint

import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.politics
import votelog.domain.politics.Person

trait PersonStoreEndpoint
  extends ReadOnlyStoreEndpoint {

  override type Entity = Person
  override type Id = Person.Id
  override type Context = politics.Context
  override type IndexOptions = IndexQueryParameters[politics.Context, Person.Field, Person.Field]

}
