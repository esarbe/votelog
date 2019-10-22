package votelog.service

import cats.effect.IO
import votelog.circe.implicits._
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Motion
import votelog.infrastructure.{ReadOnlyStoreAlg, ReadOnlyStoreService, StoreAlg}

class MotionService(
  val component: Component,
  val store: ReadOnlyStoreAlg[IO, Motion, Motion.Id],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Motion, Motion.Id]
