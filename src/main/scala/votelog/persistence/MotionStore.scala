package votelog.persistence

import java.util.UUID

import votelog.domain.politics.Politician
import votelog.domain.politics.Motion
import votelog.infrastructure.StoreAlg

trait MotionStore[F[_]] extends StoreAlg[F, Motion, Motion.Id, MotionStore.Recipe]

object MotionStore {

  case class Recipe(name: String, submitter: Politician.Id)

  def newId: Motion.Id = Motion.Id(UUID.randomUUID())
}
