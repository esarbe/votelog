package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import io.circe.{KeyDecoder, KeyEncoder}
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Motion, Person, Votum}
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{ReadOnlyStoreService, VoteAlg}
import votelog.orphans.circe.implicits._

class PersonService(
  val component: Component,
  val store: ReadOnlyStoreAlg[IO, Person, Person.Id],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Person, Person.Id] {

  implicit val motionIdCirceKeyEncoder: KeyEncoder[Motion.Id] =
    KeyEncoder.instance[Motion.Id](_.value.toString)

  object MotionId {
    def unapply(str: String): Option[Motion.Id] =
      KeyDecoder[Motion.Id].apply(str)
  }

  lazy val voting: AuthedRoutes[User, IO] =  AuthedRoutes.of {

    case GET -> Root / Id(id) / "votes" as user =>
      voteAlg.getVotesForPerson(id).attempt.flatMap {
        case Right(votes) => Ok(votes.toMap.asJson)
        case Left(error) => InternalServerError(error.getMessage)
      }
  }

  override def service: AuthedRoutes[User, IO] = super.service <+> voting
}
