package votelog.domain.model

import votelog.domain.model.Politician.Id
import votelog.infrastructure.Identified

import scala.util.Try

case class Politician(id: Id, name: String)
case class Politician2(name: String, foo: Int)

object Politician {
  case class Id(value: Long) extends AnyVal

  implicit object PoliticianIdentified extends Identified[Politician] {
    override type Identity = Id

    override def identity(t: Politician): Identity = t.id
  }
}
