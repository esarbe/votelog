package votelog.service

import cats.effect.IO
import votelog.infrastructure.{StoreAlg, StoreService}
import votelog.persistence.MotionStore
import votelog.circe.implicits._
import votelog.domain.authorization.{AuthAlg, Component}
import votelog.domain.politics.Motion
import votelog.implicits._

class MotionService(
  val component: Component,
  val store: StoreAlg[IO, Motion, Motion.Id, MotionStore.Recipe],
  val authAlg: AuthAlg[IO],
) extends StoreService[Motion, Motion.Id, MotionStore.Recipe]
