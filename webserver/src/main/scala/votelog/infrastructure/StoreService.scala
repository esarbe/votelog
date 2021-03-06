package votelog
package infrastructure

import cats.effect._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, KeyDecoder}
import org.http4s.EntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{AuthedRoutes, _}
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.crudi.StoreAlg
import votelog.domain.param

// TODO: it would be nice for testing if StoreService had a type parameter for the effect type
abstract class StoreService[
  T: Encoder: Decoder,
  Identity: Encoder: KeyDecoder,
  Recipe: Decoder,
  Partial: Encoder,
  ReadParameters,
  IndexParameters](
  implicit indexEncoder: Encoder[Index[Identity, Partial]]
) {
  val authAlg: AuthorizationAlg[IO]
  val store: StoreAlg[IO, T, Identity, Recipe, Partial, ReadParameters, IndexParameters]
  val component: Component

  implicit val queryParamDecoder: param.Decoder[ReadParameters]
  implicit val indexQueryParamDecoder: param.Decoder[IndexParameters]

  object iqp extends QueryParameterExtractor[IndexParameters]
  object qp extends QueryParameterExtractor[ReadParameters]

  object Id {
    def unapply(str: String): Option[Identity] =
      KeyDecoder[Identity].apply(str)
  }

  def checkAuthorization(
      user: User,
      capability: Capability,
      component: Component)(
      req: IO[Response[IO]]
  ): IO[Response[IO]] = {
    authAlg.hasCapability(user, capability, component).flatMap {
      isAuthorized =>
        if (isAuthorized) req
        else Forbidden(s"no permission to $capability $component")
    }
  }

  def service: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root :? iqp(params) as user =>
      checkAuthorization(user, Capability.Read, component) {
        store.index(params).flatMap(id => Ok(id.asJson))
      }

    case GET -> Root / Id(id) :? qp(params) as user =>
      checkAuthorization(user, Capability.Read, component.child(id.toString)) {
        store.read(params)(id).attempt.flatMap {
          case Right(e) => Ok(e.asJson)
          case Left(e) => NotFound(e.getMessage)
        }
      }

    case req @ POST -> Root as user =>
      checkAuthorization(user, Capability.Create, component) {
        req.req
          .as[Recipe]
          .flatMap(store.create)
          .attempt
          .flatMap {
            case Right(id) => Created(id.asJson)
            case Left(e) => InternalServerError(e.getMessage)
          }
      }

    case req @ PUT -> Root / Id(id) as user =>
      checkAuthorization(user, Capability.Update, component.child(id.toString)) {
        req.req
          .as[Recipe]
          .flatMap(t => store.update(id, t))
          .attempt
          .flatMap {
            case Right(e) => Ok(e.asJson)
            case Left(e) => InternalServerError(e.getMessage)
          }
      }

    case DELETE -> Root / Id(id) as user =>
      checkAuthorization(user, Capability.Delete, component.child(id.toString)) {
        store
          .delete(id)
          .attempt
          .flatMap {
            case Right(()) => NoContent()
            case Left(e) => InternalServerError(e.getMessage)
          }
      }
  }
}
