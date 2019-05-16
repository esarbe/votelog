package votelog.implementation

import cats.Applicative
import votelog.domain.authorization.{AuthorizationAlg, Capability, Component, User}

class UserCapabilityAuthorization[F[_]: Applicative] extends AuthorizationAlg[F] {
  def hasCapability(user: User, capability: Capability, component: Component): F[Boolean] =
    Applicative[F].pure(
      user
        .permissions
        .filter(_.component.contains(component))
        .map(_.capability)
        .contains(capability)
    )
}