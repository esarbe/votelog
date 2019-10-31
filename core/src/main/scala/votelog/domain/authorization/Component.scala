package votelog.domain.authorization

import votelog.domain.authorization.Component.Separator

/**
  * Components form a n-tree where each component
  * is either a root or a descendant of another component
  * if a component a is a descendant of another component,
  * it is said to be contained in that component
  *
  * the encoding of the tree-structure currently happens
  * as part of the name.
  * The name consists of the full component path with the
  * simple name of the simple name as last element.
  *
  * elements are separated by dots (.).
  */
case class Component(location: String) extends AnyVal {
  def contains(other: Component): Boolean = {
    val top =
      location.split(Separator)
        .zip(other.location.split(Separator))

    val compared = top.map { case (a, b) => a == b }

    location.length < other.location.length &&
      compared.forall(identity)
  }

  def child(name: String): Component = Component(s"$location$Separator$name")
  def name: String = location.split(Separator).lastOption.getOrElse(s"")
}

object Component {
  val Root = Component("")
  private val Separator: Char = '/'
}
