package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import io.circe._

import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Language, Motion, Person, Votum}
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{Param, ReadOnlyStoreService, VoteAlg}
import votelog.orphans.circe.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import votelog.persistence.PersonStore
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import PersonService._


class PersonService(
  val component: Component,
  val store: PersonStore[IO],
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

  override implicit val queryParamDecoder: Param[Language] = Params.languageParam
  override implicit val indexQueryParamDecoder: Param[ReadOnlyStoreAlg.IndexQueryParameters[Context]] =
    Params.indexQueryParam(Params.contextParam)
}

object PersonService {
  implicit val pencoder: Encoder[Person] = implicitly[Encoder[Person]]
  implicit val pdecoder: Decoder[Person] = implicitly[Decoder[Person]]
}