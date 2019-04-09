package votelog.service

import cats.effect.IO
import io.circe
import cats.implicits._
import io.circe.generic.decoding.DerivedDecoder
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import shapeless.Lazy
import votelog.app.Webserver.log
import votelog.domain.model.{Motion, Politician}
import votelog.implicits.MotionIdFromStringDecoder
import votelog.infrastructure.{StoreAlg, StoreService}
import votelog.infrastructure.logging.Logger
import votelog.persistence.MotionStore

class MotionStoreService(
  val store: StoreAlg[IO, Motion, Motion.Id, MotionStore.Recipe]
) extends StoreService[Motion, Motion.Id, MotionStore.Recipe] {

  import io.circe._
  import io.circe.generic.semiauto._

  val Mount = "motion"

  implicit val IdEncoder = MotionIdFromStringDecoder
  implicit val tidEncoder: circe.Encoder[Motion.Id] = deriveEncoder[Motion.Id]
  implicit val tidDecoder: circe.Decoder[Motion.Id] = deriveDecoder[Motion.Id]
  implicit val pidDecoder: circe.Decoder[Politician.Id] = deriveDecoder[Politician.Id]
  implicit val recipeDecoder: circe.Decoder[MotionStore.Recipe] = deriveDecoder[MotionStore.Recipe]

  implicit val tDecoder: circe.Decoder[Motion] =
    deriveDecoder[(Long, String, Long)]
      .map { case (mid, name, pid) =>
        Motion(Motion.Id(mid), name, Politician.Id(pid))
      }

  implicit val tEncoder: circe.Encoder[Motion] =
    deriveEncoder[(Long, String, Long)]
      .contramap { m: Motion => (m.id.value, m.name, m.submitter.value) }


}
