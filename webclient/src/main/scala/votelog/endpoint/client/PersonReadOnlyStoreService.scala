package votelog.endpoint.client

import io.circe.parser
import org.scalajs.dom.ext.Ajax
import votelog.domain.authentication.SessionService.Error.DecodingError
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.politics.{Context, Person}
import votelog.orphans.circe.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonReadOnlyStoreService(baseUrl: String, context: Context)
  extends ReadOnlyStoreAlg[Future, Person, Person.Id] {
  override def index(queryParameters: IndexQueryParameters): Future[List[Person.Id]] =
    Ajax
      .get(baseUrl + "/person/index", withCredentials = true)
      .flatMap { res =>
        parser.decode[List[Person.Id]](res.responseText) match {
          case Right(persons) => Future.successful(persons)
          case Left(error) => Future.failed(DecodingError(error))
        }
      }

  override def read(queryParameters: QueryParameters)(id: Person.Id): Future[Person] =
    Ajax
      .get(baseUrl + s"/person/${id.value.toString}", withCredentials = true)
      .flatMap { res =>
        parser.decode[Person](res.responseText) match {
          case Right(persons) => Future.successful(persons)
          case Left(error) => Future.failed(DecodingError(error))
        }
      }
}
