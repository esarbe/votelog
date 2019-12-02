package votelog.endpoint.client

import io.circe.parser
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.domain.authentication.SessionService.Error.DecodingError
import votelog.domain.politics.Person
import votelog.orphans.circe.implicits._
import io.circe.generic.auto._
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonReadOnlyStoreAjaxService(configuration: Configuration)
  extends PersonStore[Future] {

  override def index(qp: IndexQueryParameters): Future[List[Person.Id]] = {
    val pathQps =
      Map(
        "pg" -> qp.pageSize.value,
        "os" -> qp.offset.value,
        "lang" -> qp.queryParameters.language.iso639_1,
        "lp" -> qp.queryParameters.legislativePeriod.value.toString,
      )
        .map { case (key, value) => s"$key=$value" }
        .mkString("&")

    Ajax
      .get(configuration.url + s"/person/index?" + pathQps, withCredentials = true)
      .flatMap { res =>
        parser.decode[List[Person.Id]](res.responseText) match {
          case Right(persons) => Future.successful(persons)
          case Left(error) => Future.failed(DecodingError(error))
        }
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
