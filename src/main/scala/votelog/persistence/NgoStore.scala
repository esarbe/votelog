package votelog.persistence

import java.util.UUID

import votelog.domain.politics.{Ngo, Scoring}
import votelog.infrastructure.StoreAlg
import votelog.persistence.NgoStore.Recipe

trait NgoStore[F[_]] extends StoreAlg[F, Ngo, Ngo.Id, Recipe] with Scoring[F]

object NgoStore {

  case class Recipe(name: String)

  def newId: Ngo.Id = Ngo.Id(UUID.randomUUID())
}