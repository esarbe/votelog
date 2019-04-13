package votelog.service

import cats.effect.IO
import votelog.domain.model.Motion
import votelog.infrastructure.{StoreAlg, StoreService}
import votelog.persistence.MotionStore
import votelog.circe.implicits._
import votelog.implicits._

class MotionStoreService(
  val store: StoreAlg[IO, Motion, Motion.Id, MotionStore.Recipe]
) extends StoreService[Motion, Motion.Id, MotionStore.Recipe]
