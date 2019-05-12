package votelog.domain.authorization

import votelog.domain.authorization.User.Permission

case class User(name: String, email: User.Email, hashedPassword: String, permissions: Set[Permission])

object User {
  case class Permission(capability: Capability, component: Component)
  case class Id(value: Long) extends AnyVal
  case class Email(value: String) extends AnyVal
}
