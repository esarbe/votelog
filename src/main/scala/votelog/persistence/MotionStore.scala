package votelog.persistence

import votelog.domain.politics.Motion
import votelog.infrastructure.ReadOnlyStoreAlg

trait MotionStore[F[_]] extends ReadOnlyStoreAlg[F, Motion, Motion.Id]
