package votelog.client.service

import io.circe.generic.auto._
import io.circe.{Decoder, parser}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.client.service.params.Politics._
import votelog.domain.politics.{Context, Language, Person}
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonStoreXhr(configuration: Configuration)
  extends PersonStore[Future] {

  override def index(qp: IndexQueryParameters): Future[List[Person.Id]] = {
    Ajax
      .get(configuration.url + s"/person/?" + indexQueryParam(qp), withCredentials = true)
      .flatMap(ifSuccess(asJson[List[Person.Id]]))
  }

  override def read(queryParameters: Language)(id: Person.Id): Future[Person] =
    Ajax
      .get(configuration.url + s"/person/${id.value.toString}?lang=${queryParameters.iso639_1}", withCredentials = true)
      .flatMap(ifSuccess(asJson[Person]))

  def asJson[T: Decoder](res: XMLHttpRequest): Future[T] =
    parser.decode[T](res.responseText).fold(Future.failed, Future.successful)

  def ifSuccess[T](f: XMLHttpRequest => Future[T])(res: XMLHttpRequest): Future[T] =  {
    if (200 <= res.status && res.status < 300) f(res)
    else Future.failed(new RuntimeException(res.responseText))
  }
}
