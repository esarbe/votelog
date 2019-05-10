package votelog.domain.authorization

import cats.Show
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
case class Component(name: String) extends AnyVal {
  def contains(other: Component): Boolean = {
    val top =
      name.split(Separator)
        .zip(other.name.split(Separator))

    val compared = top.map { case (a, b) => a == b }

    name.length < other.name.length &&
      compared.forall(identity)
  }
}

object Component {
  val Separator: Char = '.'
}