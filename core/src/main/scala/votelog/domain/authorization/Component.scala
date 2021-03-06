package votelog.domain.authorization

import votelog.domain.authorization.Component.Separator

/**
 * TODO: rename 'Path'
 *
  * Components form a n-tree where each component
  * is either a root or a descendant of another component
  * if a component a is a descendant of another component,
  * it is said to be contained in that component
  *
  * the encoding of the tree-structure currently happens
  * as part of the location.
  * The name consists of of the last element of the full
  * component path.
  *
  * elements are separated by slashes (`/`).
  */
case class Component(location: String) extends AnyVal {
  def contains(other: Component): Boolean = {
    val top =
      location.split(Separator)
        .zip(other.location.split(Separator))

    val compared = top.map { case (a, b) => a == b }

    this != other && compared.forall(identity)
  }

  def containsOrSelf(other: Component): Boolean = contains(other) || other == this

  def child(name: String): Component = Component(s"$location$Separator$name")
  def name: String = location.split(Separator).lastOption.getOrElse(s"")
}

object Component {
  val Root = Component("")
  private val Separator: Char = '/'
}
