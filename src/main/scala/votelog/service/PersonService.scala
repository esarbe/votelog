package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import io.circe.{Encoder, KeyDecoder, KeyEncoder}
import org.http4s.{AuthedService, HttpRoutes}
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.circe.implicits._
import votelog.domain.authorization.{AuthorizationAlg, Component, User}
import votelog.domain.politics.{Motion, Person, Votum}
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{ReadOnlyStoreAlg, ReadOnlyStoreService, StoreAlg, VoteAlg}

class PersonService(
  val component: Component,
  val store: ReadOnlyStoreAlg[IO, Person, Person.Id],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Person, Person.Id] {

  implicit val motionIdCirceKeyEncoder = KeyEncoder.instance[Motion.Id](_.value.toString)
  implicit val votesByMotionIdCirceEncoder: Encoder[Map[Motion.Id, Votum]] = Encoder.encodeMap[Motion.Id, Votum]

  object MotionId {
    def unapply(str: String): Option[Motion.Id] =
      KeyDecoder[Motion.Id].apply(str)
  }

  object Votum {
    def unapply(str: String): Option[Votum] =
      KeyDecoder[Option[Votum]].apply(str).flatten
  }

  lazy val voting: AuthedService[User, IO] =  AuthedService {

    case GET -> Root / Id(id) / "votes" as user =>
      voteAlg.getVotesForPerson(id).attempt.flatMap {
        case Right(votes) => Ok(votes.toMap.asJson)
        case Left(error) => InternalServerError(error.getMessage)
      }
  }

  override def service: AuthedService[User, IO] = super.service <+> voting
}
