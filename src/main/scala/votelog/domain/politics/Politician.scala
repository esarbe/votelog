package votelog.domain.politics

import votelog.infrastructure.Identified

case class Politician(id: Politician.Id, name: String, partyId: Party.Id)

object Politician {
  case class Id(value: Long)

  implicit object PoliticianIdentified extends Identified[Politician] {
    override type Identity = Id

    override def identity(t: Politician): Identity = t.id
  }
}
