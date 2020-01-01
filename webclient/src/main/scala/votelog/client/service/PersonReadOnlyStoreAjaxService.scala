package votelog.client.service

import io.circe.generic.auto._
import io.circe.{Decoder, parser}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.domain.politics.{Language, Person}
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonReadOnlyStoreAjaxService(configuration: Configuration)
  extends PersonStore[Future] {

  override def index(qp: IndexQueryParameters): Future[List[Person.Id]] = {
    val pathQps =
      Map(
        "ps" -> qp.pageSize.value,
        "os" -> qp.offset.value,
        "lang" -> qp.queryParameters.language.iso639_1,
        "lp" -> qp.queryParameters.legislativePeriod.value.toString,
      )
        .map { case (key, value) => s"$key=$value" }
        .mkString("&")

    Ajax
      .get(configuration.url + s"/person/?" + pathQps, withCredentials = true)
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
