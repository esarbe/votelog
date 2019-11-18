package votelog.endpoint

import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.politics
import votelog.domain.politics.{Context, Person}
import votelog.endpoint.ReadOnlyStoreEndpoint.Paging

import scala.concurrent.Future

trait PersonStoreEndpoint
  extends ReadOnlyStoreEndpoint {

  override type Entity = politics.Person
  override type Id = politics.Person.Id
  override type IndexOptions = IndexQueryParameters
  override type Context = politics.Context
}
