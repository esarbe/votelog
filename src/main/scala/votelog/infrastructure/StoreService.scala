package votelog
package infrastructure

import cats.effect._
import io.circe.syntax._
import org.http4s.EntityEncoder._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.io._
import votelog.domain.authorization.{AuthAlg, Capability, Component, User}
import votelog.implicits._
import votelog.infrastructure.StoreService.StringEncoded

object StoreService {
  type StringEncoded[T] = encoding.Encoder[String, T]
}

abstract class StoreService[
  T: io.circe.Encoder: io.circe.Decoder,
  Identity: io.circe.Encoder: StringEncoded,
  Recipe: io.circe.Decoder
]
{
  val authAlg: AuthAlg[IO]
  val store: StoreAlg[IO, T, Identity, Recipe]
  val component: Component

  object Id {
    def unapply(str: String): Option[Identity] =
      str.encodeAs[Identity].toOption
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

  def service: AuthedService[User, IO] = AuthedService {
    case GET -> Root / "index" as user =>
      checkAuthorization(user, Capability.Read, component) {
        store.index.flatMap(id => Ok(id.asJson))
      }

    case GET -> Root / Id(id) as user =>
      checkAuthorization(user, Capability.Read, component.child(id.toString)) {
        store.read(id).attempt.flatMap {
          case Right(e) => Ok(e.asJson)
          case Left(e) => NotFound(e.getMessage)
        }
      }

    case req @ POST -> Root / "create" as user =>
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
