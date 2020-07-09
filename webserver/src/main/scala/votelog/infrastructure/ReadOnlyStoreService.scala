package votelog.infrastructure

import cats.effect._
import io.circe.{Decoder, Encoder, KeyDecoder}
import io.circe.syntax._
import org.http4s.EntityEncoder._
import org.http4s.{AuthedRoutes, _}
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.param
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.Index

// TODO: it would be nice for testing if StoreService had a type parameter for the effect type
abstract class ReadOnlyStoreService[T: Encoder: Decoder, Identity: Encoder: KeyDecoder](
  implicit val indexEncoder: Encoder[Index[Identity]]
) {

  val store: ReadOnlyStoreAlg[IO, T, Identity]
  implicit val queryParamDecoder: param.Decoder[store.QueryParameters]
  implicit val indexQueryParamDecoder: param.Decoder[store.IndexQueryParameters]

  val authAlg: AuthorizationAlg[IO]
  val component: Component

  object Id {
    def unapply(str: String): Option[Identity] =
      KeyDecoder[Identity].apply(str)
  }

  object iqp extends QueryParameterExtractor[store.IndexQueryParameters]
  object qp extends QueryParameterExtractor[store.QueryParameters]

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
        store.index(params).flatMap(index => Ok(index.asJson))
      }

    case GET -> Root / Id(id) :? qp(params) as user =>
      checkAuthorization(user, Capability.Read, component.child(id.toString)) {
          store.read(params)(id).attempt.flatMap {
            case Right(e) => Ok(e.asJson)
            case Left(e) => NotFound(e.getMessage)
        }
      }
  }
}
