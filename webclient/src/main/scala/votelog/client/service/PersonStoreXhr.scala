package votelog.client.service

import io.circe.generic.auto._
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import AjaxRequest.{fromJson, ifSuccess}
import votelog.client.service.params.Politics.{contextParamEncoder, _}
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Language, Person}
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore
import votelog.domain.param.Encoder._

import votelog.client.service.params.Politics._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonStoreXhr(configuration: Configuration)
  extends PersonStore[Future] {

  override def index(qp: IndexParameters): Future[Index[Person.Id]] = {
    Ajax
      .get(configuration.url + s"/person/" + qp.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[Index[Person.Id]]))
  }

  override def read(language: Language)(id: Person.Id): Future[Person] =
    Ajax
      .get(configuration.url + s"/person/" + id.value + language.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[Person]))
}
