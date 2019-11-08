package votelog.persistence

import java.util.UUID

import cats.effect.IO
import io.chrisdavenport.fuuid.FUUID
import votelog.domain.crudi.StoreAlg
import votelog.domain.politics.{Ngo, Scoring}
import votelog.persistence.NgoStore.Recipe

//TODO: Scoring should be it's own store
trait NgoStore[F[_]] extends StoreAlg[F, Ngo, Ngo.Id, Recipe] with Scoring[F]

object NgoStore {

  case class Recipe(name: String)

  // TODO: remove unsafeRunSync
  def newId: Ngo.Id = FUUID.randomFUUID[IO].map(Ngo.Id).unsafeRunSync()
}