package votelog.domain.authorization

// maybe: argument type in place of user
trait AuthorizationAlg[F[_]] {
  def hasCapability(user: User, capability: Capability, component: Component): F[Boolean]
}
