package votelog.client.service

import io.circe.syntax._
import org.scalajs.dom.ext.Ajax
import votelog.client.Configuration
import votelog.client.service.AjaxRequest.{fromJson, ifSuccess}
import votelog.client.service.ReadOnlyStoreXhr.indexQueryParam
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, VoteAlg, Votum}
import io.circe.Decoder
import votelog.client.service.params.Politics._
import votelog.orphans.circe.implicits._
import votelog.domain.param.Encoder._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class VoteStoreXhr(configuration: Configuration) extends VoteAlg[Future] {

  override def getVotesForBusiness(context: Context)(business: Business.Id): Future[List[(Person.Id, Votum)]] = {
    Ajax
      .get(configuration.url + s"/person/$business/votes?" + context.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[List[(Person.Id, Votum)]]))
  }

  override def getVotesForPerson(context: Context)(person: Person.Id): Future[List[(Business.Id, Votum)]] = {
    Ajax
      .get(configuration.url + s"/person/$person/votes?" + context.urlEncode, withCredentials = true)
      .flatMap(ifSuccess(fromJson[List[(Business.Id, Votum)]]))
  }
}
