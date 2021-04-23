package votelog.domain.authentication

import votelog.domain.authentication.User.Permission
import votelog.domain.authorization.{Capability, Component}

case class User(name: String, email: User.Email, passwordHash: String, permissions: Set[Permission])

object User {

  case class Partial(
    name: Option[String],
    email: Option[User.Email],
  )

  val empty = Partial(None, None)
  case class Permission(capability: Capability, component: Component)

  case class Id(value: String) extends AnyVal
  case class Email(value: String) extends AnyVal

  sealed trait Field extends Product
  object Field {
    case object Name extends Field
    case object Email extends Field

    val values = List(Name, Email)
    val fromString: (String => Field) =
      (values zip values).map( { case (key, value) => (key.toString, value) }).toMap.apply
  }
}
