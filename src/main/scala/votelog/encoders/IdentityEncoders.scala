package votelog.encoders

import votelog.domain.authorization.User
import votelog.domain.politics.{Motion, Politician}
import votelog.infrastructure.encoding.Encoder

import scala.util.Try

trait IdentityEncoders {
  implicit val MotionIdFromStringEncoder: Encoder[String, Motion.Id] =
    (a: String) => Try(a.toLong).map(Motion.Id).toEither

  implicit val PoliticianIdFromStringEncoder: Encoder[String, Politician.Id] =
    (a: String) => Try(a.toLong).map(Politician.Id).toEither

  implicit val userIdFromStringEncoder: Encoder[String, User.Id] =
    (a: String) => Try(a.toLong).map(User.Id).toEither
}
