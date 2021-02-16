package votelog.service

import cats.effect.IO
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.dsl.io._
import org.http4s.circe._
import votelog.domain.param
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.{Business, Context, Language, Person, VoteAlg, Votum}
import votelog.infrastructure.ReadOnlyStoreService
import votelog.persistence.BusinessStore
import votelog.orphans.circe.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters

class BusinessService(
  val component: Component,
  val store: BusinessStore[IO],
  val authAlg: AuthorizationAlg[IO],
  val voteAlg: VoteAlg[IO],
) extends ReadOnlyStoreService[Business, Business.Id, Business.Partial, Business.Field, Business.Field] {

  implicit val orderingParamDecoder: param.Decoder[List[Business.Field]] = Params.orderDecoder
  implicit val contextParamDecoder: param.Decoder[Context] = Params.contextParam
  override implicit val queryParamDecoder: param.Decoder[Language] = Params.languageParam
  override implicit val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Context, Business.Field, Business.Field]] =
    Params.indexParamsDecoder(contextParamDecoder, orderingParamDecoder)

  lazy val voting: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root / Id(id) / "votes" :? iqp(params) as user =>
      voteAlg
        .getVotesForBusiness(params.indexContext)(id)
        .attempt.flatMap {
          case Right(votes) => Ok(votes.toMap.asJson)
          case Left(error) => InternalServerError(error.getMessage)
        }
  }
}
