package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Ngo
import votelog.infrastructure.{StoreAlg, StoreService}
import votelog.persistence.NgoStore
import votelog.circe.implicits._

class NgoService(
  val component: Component,
  val store: StoreAlg[IO, Ngo, Ngo.Id, NgoStore.Recipe],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe]
