package votelog.client.service

import cats._
import cats.implicits._
import io.circe.generic.auto._
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import AjaxRequest.{fromJson, ifSuccess}
import io.circe.{KeyDecoder, KeyEncoder}
import votelog.client.service.params.Politics.{contextParamEncoder, _}
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.politics.{Context, Language, Person, PersonPartial}
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore
import votelog.domain.param.Encoder._
import votelog.client.service.params.Politics._
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.data.Sorting
import votelog.domain.param.{Encoder, Params}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonStoreXhr(configuration: Configuration)
  extends PersonStore[Future] {

  implicit val indexParamEncoder: Encoder[IndexQueryParameters[Context, Person.Field, Person.Field]] =
    new Encoder[IndexQueryParameters[Context, Person.Field, Person.Field]] {
      override def encode(qp: ReadOnlyStoreAlg.IndexQueryParameters[Context, Person.Field, Person.Field]): Params = {
        Params(Map(
          "ps" -> Seq(qp.pageSize.value.toString),
          "os" -> Seq(qp.offset.value.toString),
          "lang" -> Seq(qp.indexContext.language.iso639_1),
          "lp" -> Seq(qp.indexContext.legislativePeriod.value.toString),
          "fields" -> qp.fields.map(KeyEncoder[Person.Field].apply),
        )) |+| orderEncoder(qp.orderings)
      }
    }

  override def index(qp: IndexQueryParameters[Context, Person.Field, Person.Field]): Future[Index[Person.Id, PersonPartial]] = {
    Ajax
      .get(configuration.url + s"/person/" + qp.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[Index[Person.Id, PersonPartial]]))
  }

  override def read(language: Language)(id: Person.Id): Future[Person] =
    Ajax
      .get(configuration.url + s"/person/" + id.value + language.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[Person]))
}
