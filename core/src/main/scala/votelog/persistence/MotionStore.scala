package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.Motion

trait MotionStore[F[_]] extends ReadOnlyStoreAlg[F, Motion, Motion.Id]
