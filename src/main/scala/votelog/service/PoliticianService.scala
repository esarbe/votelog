package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import votelog.domain.model.{Motion, Politician, Votum}
import votelog.implicits.{PoliticianIdFromStringDecoder, _}
import votelog.infrastructure.encoding.Encoder
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{StoreAlg, StoreService, VoteAlg}
import votelog.persistence.PoliticianStore

class PoliticianService(
  val store: StoreAlg[IO, Politician, Politician.Id, PoliticianStore.Recipe],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
) extends StoreService[Politician, Politician.Id, PoliticianStore.Recipe] {

  import io.circe.generic.semiauto._

  val Mount = "politician"

  implicit val IdEncoder: Encoder[String, Politician.Id] = PoliticianIdFromStringDecoder

  implicit val tidEncoder: circe.Encoder[Politician.Id] = deriveEncoder[Politician.Id]
  implicit val tidDecoder: circe.Decoder[Politician.Id] = deriveDecoder[Politician.Id]
  implicit val recipeDecoder: circe.Decoder[PoliticianStore.Recipe] = deriveDecoder[PoliticianStore.Recipe]
  implicit val tEncoder: circe.Encoder[Politician] = deriveEncoder[Politician]
  implicit val tDecoder: circe.Decoder[Politician] = deriveDecoder[Politician]

  override def service: HttpRoutes[IO] = super.service <+> voting

  object MotionId {
    def unapply(str: String): Option[Motion.Id] =
      str.encodeAs[Motion.Id].toOption
  }

  object Votum {
    def unapply(str: String): Option[Votum] =
      str.encodeAs[Votum].toOption
  }

  val voting: HttpRoutes[IO] =  HttpRoutes.of[IO] {
    case POST -> Root / Mount / Id(id) / "motion" / MotionId(motionId) / "vote" / Votum(votum) =>
      voteAlg.voteFor(id, motionId, votum) *> Ok("")
  }
}
