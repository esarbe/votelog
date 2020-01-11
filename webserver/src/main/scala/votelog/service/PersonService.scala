package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, Language, LegislativePeriod, Business, Person}
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{Param, ReadOnlyStoreService, VoteAlg}
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore

class PersonService(
  val component: Component,
  val store: PersonStore[IO],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Person, Person.Id] {

  implicit val motionIdCirceKeyEncoder: KeyEncoder[Business.Id] =
    KeyEncoder.instance[Business.Id](_.value.toString)

  val defaultIndexQueryParameters: IndexQueryParameters[Context] =
    IndexQueryParameters[Context](PageSize(20), Offset(0), Context(LegislativePeriod.Id(50), Language.English))

  object MotionId {
    def unapply(str: String): Option[Business.Id] =
      KeyDecoder[Business.Id].apply(str)
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
  override implicit val indexQueryParamDecoder: Param[IndexQueryParameters[Context]] =
    iqpc => Params.indexQueryParam(Params.contextParam).decode(iqpc)
      .orElse(Some(defaultIndexQueryParameters)) // TODO: redirect in case of missing required parameters would be better
}
