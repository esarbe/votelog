package votelog.service

import cats.effect.IO
import votelog.orphans.circe.implicits._
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.Motion
import votelog.infrastructure.ReadOnlyStoreService

class MotionService(
  val component: Component,
  val store: ReadOnlyStoreAlg[IO, Motion, Motion.Id],
  val authAlg: AuthorizationAlg[IO],
) extends ReadOnlyStoreService[Motion, Motion.Id]
