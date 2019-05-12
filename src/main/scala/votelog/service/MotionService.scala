package votelog.service

import cats.effect.IO
import votelog.infrastructure.{StoreAlg, StoreService}
import votelog.persistence.MotionStore
import votelog.circe.implicits._
import votelog.domain.politics.Motion
import votelog.implicits._

abstract class MotionService(
  val store: StoreAlg[IO, Motion, Motion.Id, MotionStore.Recipe]
) extends StoreService[Motion, Motion.Id, MotionStore.Recipe]
