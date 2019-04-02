package votelog.implementation

import doobie.util.Meta
import votelog.domain.model.Politician
import votelog.infrastructure.Identified

object IdentifiedPolitician extends Identified[Politician] {
  override type Identity = Politician.Id

  override def identity(t: Politician): Identity = t.id

  val MetaIdentity: Meta[Identity] = Meta[Long].imap(Politician.Id)(_.value)
}
