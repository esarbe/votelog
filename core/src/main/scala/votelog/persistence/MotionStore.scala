package votelog.persistence

import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.politics.{Context, Motion}

trait MotionStore[F[_]] extends ReadOnlyStoreAlg[F, Motion, Motion.Id] {
  type QueryParameters = Context
  type IndexQueryParameters = ReadOnlyStoreAlg.IndexQueryParameters[Context]
}


