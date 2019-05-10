package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import io.circe.{Encoder, KeyEncoder}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.circe.implicits._
import votelog.domain.politics.{Motion, Politician, Votum}
import votelog.implicits._
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{StoreAlg, StoreService, VoteAlg}
import votelog.persistence.PoliticianStore

class PoliticianService(
  val store: StoreAlg[IO, Politician, Politician.Id, PoliticianStore.Recipe],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
) extends StoreService[Politician, Politician.Id, PoliticianStore.Recipe] {

  override def service: HttpRoutes[IO] = super.service <+> voting

  implicit val motionIdCirceKeyEncoder = KeyEncoder.instance[Motion.Id](_.value.toString)
  implicit val votesByMotionIdCirceEncoder: Encoder[Map[Motion.Id, Votum]] = Encoder.encodeMap[Motion.Id, Votum]


  object MotionId {
    def unapply(str: String): Option[Motion.Id] =
      str.encodeAs[Motion.Id].toOption
  }

  object Votum {
    def unapply(str: String): Option[Votum] =
      str.encodeAs[Votum].toOption
  }

  val voting: HttpRoutes[IO] =  HttpRoutes.of[IO] {
    case POST -> Root /  Id(id) / "motion" / MotionId(motionId) / "vote" / Votum(votum) =>
      voteAlg.voteFor(id, motionId, votum) *> Ok("")

    case GET -> Root / Id(id) / "votes" =>
      voteAlg.getVotes(id).attempt.flatMap {
        case Right(votes) => Ok(votes.toMap.asJson)
        case Left(error) => InternalServerError(error.getMessage)
      }
  }
}
