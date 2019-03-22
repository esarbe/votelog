package votelog.implementation

import votelog.domain.model.Politician
import votelog.infrastructure.Identified

object IdentifiedPolitician extends Identified[Politician] {
  override type Identity = Politician.Id

  override def identity(t: Politician): Identity = t.id
}
