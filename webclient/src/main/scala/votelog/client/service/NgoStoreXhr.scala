package votelog.client.service

import votelog.client.Configuration
import votelog.domain.politics.{Business, Ngo, Scoring}
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.param.Encoder

import scala.concurrent.Future

class NgoStoreXhr(configuration: Configuration)
  extends StoreXhr[Ngo, Ngo.Id, NgoStore.Recipe, Ngo.Partial,Ngo.Fields, Ngo.Fields]
    with NgoStore[Future] {

  override val indexUrl: String = configuration.url + "/ngo"
  override implicit val indexQueryParameterEncoder: Encoder[Unit] = Encoder.unit
  override implicit val queryParameterEncoder: Encoder[Unit] = Encoder.unit

  override def motionsScoredBy(ngo: Ngo.Id): Future[List[(Business.Id, Scoring.Score)]] = ???
  override def scoreMotion(ngo: Ngo.Id, motion: Business.Id, score: Scoring.Score): Future[Unit] = ???
  override def removeMotionScore(ngo: Ngo.Id, motion: Business.Id): Future[Unit] = ???
}
