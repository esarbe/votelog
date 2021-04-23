package votelog.service

import cats.effect.IO
import io.circe.KeyDecoder
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
import votelog.domain.data.Sorting
import votelog.domain.param.Params

class BusinessService(
  val component: Component,
  val store: BusinessStore[IO],
  val authAlg: AuthorizationAlg[IO],
  val voteAlg: VoteAlg[IO],
) extends ReadOnlyStoreService[Business, Business.Id, Business.Partial, Language, IndexQueryParameters[Context, Business.Field, Business.Field]] {

  override implicit val queryParamDecoder: param.Decoder[Language] = ParamDecoders.languageParam
  override implicit val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Context, Business.Field, Business.Field]] =
    ParamDecoders.indexParamsDecoder(
      ParamDecoders.contextParam,
      ParamDecoders.orderDecoder,
      ParamDecoders.fieldsDecoder
    )

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
