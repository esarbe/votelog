package votelog.domain.authorization

// maybe: argument type in place of user
trait AuthAlg[F[_]] {
  def hasCapability[C](user: User, capability: Capability, component: Component): F[Boolean]
}
