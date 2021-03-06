package votelog.service

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, PersonPartial, VoteAlg}
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.param
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction.Descending
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.ReadOnlyStoreService
import votelog.orphans.circe.implicits._
import votelog.persistence.PersonStore

class PersonService(
  val component: Component,
  val store: PersonStore[IO],
  val voteAlg: VoteAlg[IO],
  val log: Logger[IO],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Person, Person.Id, PersonPartial, Language, IndexQueryParameters[Context, Person.Field, Person.Field]] {

  implicit val motionIdCirceKeyEncoder: KeyEncoder[Business.Id] =
    KeyEncoder.instance[Business.Id](_.value.toString)

  val defaultIndexQueryParameters: IndexQueryParameters[Context, Person.Field, Person.Field] =
    IndexQueryParameters(
      PageSize(20),
      Offset(0),
      Context(LegislativePeriod.Id(50), Language.English),
      List(
        Person.Field.LastName -> Descending,
        Person.Field.FirstName -> Descending,
        Person.Field.DateOfBirth -> Descending,
        Person.Field.Id -> Descending),
      Set.empty
    )

  object MotionId {
    def unapply(str: String): Option[Business.Id] =
      KeyDecoder[Business.Id].apply(str)
  }

  lazy val voting: AuthedRoutes[User, IO] =  AuthedRoutes.of {

    case GET -> Root / Id(id) / "votes" :? iqp(params) as user =>
      voteAlg
        .getVotesForPerson(params.indexContext)(id)
        .attempt.flatMap {
          case Right(votes) => Ok(votes.toMap.asJson)
          case Left(error) => InternalServerError(error.getMessage)
        }
  }

  override def service: AuthedRoutes[User, IO] = super.service <+> voting

  override implicit val queryParamDecoder: param.Decoder[Language] = Params.languageParam

  // TODO: redirect in case of missing required parameters would be better
  override implicit val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Context, Person.Field, Person.Field]] =
    params => Params.indexParamsDecoder[votelog.domain.politics.Context, Person.Field, Person.Field](Params.contextParam, Params.orderDecoder[Person.Field]).decode(params)
      .orElse(Some(defaultIndexQueryParameters))
}
