package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Ngo
import votelog.infrastructure.StoreService
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.crudi.StoreAlg

class NgoService(
  val component: Component,
  val store: StoreAlg[IO, Ngo, Ngo.Id, NgoStore.Recipe],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe]
