package votelog.endpoint.client

import endpoints.xhr
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.Error.InvalidId
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.politics.{Context, Language, Person}
import votelog.endpoint.PersonStoreEndpoint
import votelog.endpoint.ReadOnlyStoreEndpoint.Paging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PersonReadOnlyStoreService(endpoint: PersonStoreXhrEndpoint)
  extends ReadOnlyStoreAlg[Future, Person, Person.Id] {
  override def index(params: IndexQueryParameters): Future[List[Person.Id]] =
    endpoint
      .index((Paging(params.offset.value, params.pageSize.value), Context(2019, Language.English)))

  override def read(queryParameters: QueryParameters)(id: Person.Id): Future[Person] =
    endpoint
      .read((id, Context(2019, Language.English)))
      .flatMap {
        case Some(person) => Future.successful(person)
        case None => Future.failed(InvalidId(s"No person found for id$id", id))
      }
}
