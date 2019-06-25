package votelog.persistence

import cats.effect.Bracket

import scala.language.higherKinds

package object doobie {
  type ThrowableBracket[F[_]] = Bracket[F, Throwable]
}
