package votelog.decoders

import votelog.domain.model.{Motion, Politician}
import votelog.infrastructure.encoding.Encoder

import scala.util.Try

trait IdentityDecoders {
  implicit val MotionIdFromStringDecoder: Encoder[String, Motion.Id] =
    (a: String) => Try(a.toLong).map(Motion.Id).toEither

  implicit val PoliticianIdFromStringDecoder: Encoder[String, Politician.Id] =
    (a: String) => Try(a.toLong).map(Politician.Id).toEither
}
