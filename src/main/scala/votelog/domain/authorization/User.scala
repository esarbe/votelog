package votelog.domain.authorization

import java.util.UUID

import votelog.domain.authorization.User.Permission

case class User(name: String, email: User.Email, passwordHash: String, permissions: Set[Permission])

object User {
  case class Permission(capability: Capability, component: Component)

  case class Id(value: UUID) extends AnyVal
  case class Email(value: String) extends AnyVal
}
