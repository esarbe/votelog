package votelog
package infrastructure

import cats.effect._
import io.circe.{Encoder, _}
import io.circe.syntax._
import org.http4s.EntityEncoder._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.io._


trait StoreService[T, Identity, Recipe] {
  val store: StoreAlg[IO, T, Identity, Recipe]
  val Mount: String

  val IdEncoder: encoding.Encoder[String, Identity]

  implicit val tidEncoder: Encoder[Identity]
  implicit val tEncoder: Encoder[T]
  implicit val tDecoder: Decoder[T]
  implicit val recipeDecoder: Decoder[Recipe]


  object Id {
    def unapply(str: String): Option[Identity] =
      IdEncoder.encode(str).toOption
  }


  def service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / Mount / "index" =>
      store.index.flatMap(id => Ok(id.asJson))

    case GET -> Root / Mount / Id(id) =>
      store.read(id).attempt.flatMap {
        case Right(e) => Ok(e.asJson)
        case Left(e) => NotFound(e.getMessage)
      }

    case req @ POST -> Root / Mount / "create" =>
      req
        .as[Recipe]
        .flatMap(store.create)
        .attempt
        .flatMap {
          case Right(id) => Created(id.asJson)
          case Left(e) => InternalServerError(e.getMessage)
        }

    case req @ POST -> Root / Mount / Id(id) =>
      req.as[T]
        .flatMap(t => store.update(id, t))
        .attempt
        .flatMap {
          case Right(e) => Ok(e.asJson)
          case Left(e) => InternalServerError(e.getMessage)
        }

      case DELETE -> Root / Mount / Id(id) =>
      store
        .delete(id)
        .attempt
        .flatMap {
          case Right(()) => NoContent()
          case Left(e) => InternalServerError(e.getMessage)
        }

  }
}
