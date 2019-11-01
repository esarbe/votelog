package votelog.domain.authorization

import votelog.domain.authentication.User

// maybe: argument type in place of user
trait AuthorizationAlg[F[_]] {
  def hasCapability(user: User, capability: Capability, component: Component): F[Boolean]
}
