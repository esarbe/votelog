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
import votelog.infrastructure.logging.Logger


trait RestService[T] {
  val crud: CrudService[IO, T]
  val Mount: String
  val Log: Logger[IO]

  implicit val IdEncoder: encoding.Encoder[String, crud.Identity]
  implicit val tidEncoder: Encoder[crud.Identity]
  implicit val tEncoder: Encoder[T]
  implicit val tDecoder: Decoder[T]
  implicit val recipeDecoder: Decoder[crud.Recipe]


  object Id {
    def unapply(str: String): Option[crud.Identity] =
      IdEncoder.encode(str).toOption
  }


  val service = HttpRoutes.of[IO] {
    case GET -> Root / Mount / "index" =>
      crud.index.flatMap(id => Ok(id.asJson))

    case GET -> Root / Mount / Id(id) =>
      crud.read(id).attempt.flatMap {
        case Right(e) => Ok(e.asJson)
        case Left(e) => NotFound(e.getMessage)
      }

    case req @ POST -> Root / Mount / "create" =>
      req
        .as[crud.Recipe]
        .flatMap(crud.create)
        .attempt
        .flatMap {
          case Right(id) => Created(id.asJson)
          case Left(e) => InternalServerError(e.getMessage)
        }

    case req @ POST -> Root / Mount / Id(id) =>
      req.as[T]
        .flatMap(t => crud.update(id, t))
        .attempt
        .flatMap {
          case Right(e) => Ok(e.asJson)
          case Left(e) => InternalServerError(e.getMessage)
        }

      case DELETE -> Root / Mount / Id(id) =>
      crud
        .delete(id)
        .attempt
        .flatMap {
          case Right(()) => NoContent()
          case Left(e) => InternalServerError(e.getMessage)
        }

  }
}
