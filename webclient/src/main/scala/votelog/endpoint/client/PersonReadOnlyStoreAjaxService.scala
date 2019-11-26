package votelog.endpoint.client

import io.circe.parser
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.domain.authentication.SessionService.Error.DecodingError
import votelog.domain.politics.Person
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonReadOnlyStoreAjaxService(configuration: Configuration)
  extends PersonStore[Future] {
  override def index(qp: IndexQueryParameters): Future[List[Person.Id]] =
    Ajax
      .get(configuration.url + s"/person/index?pageSize=${qp.pageSize}&offset=${qp.offset}&lang=", withCredentials = true)
      .flatMap { res =>
        parser.decode[List[Person.Id]](res.responseText) match {
          case Right(persons) => Future.successful(persons)
          case Left(error) => Future.failed(DecodingError(error))
        }
      }

  override def read(queryParameters: QueryParameters)(id: Person.Id): Future[Person] =
    Ajax
      .get(configuration.url + s"/person/${id.value.toString}", withCredentials = true)
      .flatMap { res =>
        parser.decode[Person](res.responseText) match {
          case Right(persons) => Future.successful(persons)
          case Left(error) => Future.failed(DecodingError(error))
        }
      }
}
