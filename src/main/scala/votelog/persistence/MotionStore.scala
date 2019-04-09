package votelog.persistence

import votelog.domain.model.Motion.Recipe
import votelog.domain.model.{Motion, Politician}
import votelog.infrastructure.StoreAlg

trait MotionStore[F[_]] extends StoreAlg[F, Motion, Motion.Id, Recipe]

object MotionStore {
  case class Recipe(name: String, submitter: Politician.Id)
}
