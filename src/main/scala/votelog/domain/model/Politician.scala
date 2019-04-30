package votelog.domain.model

import votelog.domain.model.Politician.Id
import votelog.infrastructure.Identified

import scala.util.Try

case class Politician(id: Id, name: String, partyId: Party.Id)

object Politician {
  case class Id(value: Long)

  implicit object PoliticianIdentified extends Identified[Politician] {
    override type Identity = Id

    override def identity(t: Politician): Identity = t.id
  }
}
