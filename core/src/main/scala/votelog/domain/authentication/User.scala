package votelog.domain.authentication

import votelog.domain.authentication.User.Permission
import votelog.domain.authorization.{Capability, Component}

case class User(name: String, email: User.Email, passwordHash: String, permissions: Set[Permission])

object User {
  case class Permission(capability: Capability, component: Component)

  case class Id(value: String) extends AnyVal
  case class Email(value: String) extends AnyVal
}
