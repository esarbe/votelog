package votelog.implementation

import cats.effect.{Async, ContextShift}
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component, User}

class UserCapabilityAuthorization[F[_]: Async: ContextShift] extends AuthorizationAlg[F] {
  def hasCapability(user: User, capability: Capability, component: Component): F[Boolean] = {
    Async[F].point(
      user
        .permissions
        .filter(_.component.contains(component))
        .map(_.capability)
        .contains(capability))
      }
}