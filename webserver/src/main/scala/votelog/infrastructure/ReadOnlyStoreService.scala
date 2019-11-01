package votelog.infrastructure

import cats.effect._
import io.circe.{Decoder, Encoder, KeyDecoder}
import io.circe.syntax._
import org.http4s.EntityEncoder._
import org.http4s.{AuthedRoutes, _}
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component}
import votelog.infrastructure.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.infrastructure.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

// TODO: it would be nice for testing if StoreService had a type parameter for the
// effect type
abstract class ReadOnlyStoreService[
  T: Encoder: Decoder,
  Identity: Encoder: KeyDecoder,
]
{
  val authAlg: AuthorizationAlg[IO]
  val store: ReadOnlyStoreAlg[IO, T, Identity]
  val component: Component

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
    case GET -> Root / "index" as user =>
      checkAuthorization(user, Capability.Read, component) {
        val indexQueryParams =
          IndexQueryParameters(PageSize(0), Offset(0), QueryParameters("en"))
        store.index(indexQueryParams).flatMap(id => Ok(id.asJson))
      }

    case GET -> Root / Id(id) as user =>
      checkAuthorization(user, Capability.Read, component.child(id.toString)) {
        store.read(QueryParameters("en"))(id).attempt.flatMap {
          case Right(e) => Ok(e.asJson)
          case Left(e) => NotFound(e.getMessage)
        }
      }
  }
}
