package votelog.decoders

import votelog.domain.model.{Motion, Politician}
import votelog.infrastructure.encoding.Encoder

import scala.util.Try

trait IdentityDecoders {
  val MotionIdFromStringDecoder =
    new Encoder[String, Motion.Id] {
      override def encode(a: String): Either[Throwable, Motion.Id] =
        Try(a.toLong).map(Motion.Id).toEither
    }

  val PoliticianIdFromStringDecoder =
    new Encoder[String, Politician.Id] {
      override def encode(a: String): Either[Throwable, Politician.Id] =
        Try(a.toLong).map(Politician.Id).toEither
    }
}
