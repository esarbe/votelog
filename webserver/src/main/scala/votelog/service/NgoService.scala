package votelog.service

import cats.effect.IO
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Ngo
import votelog.infrastructure.{Param, StoreService}
import votelog.persistence.NgoStore
import votelog.orphans.circe.implicits._
import votelog.domain.crudi.StoreAlg
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._

class NgoService(
  val component: Component,
  val store: NgoStore[IO],
  val authAlg: AuthorizationAlg[IO],
) extends StoreService[Ngo, Ngo.Id, NgoStore.Recipe] {

  override implicit val queryParamDecoder: Param[store.QueryParameters] = Param.always(Unit)
  override implicit val indexQueryParamDecoder: Param[store.IndexQueryParameters] = Param.always(Unit)
}
