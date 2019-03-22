package votelog.syntax

import votelog.infrastructure.Identified


trait IdentifiedSyntax {
  implicit def syntaxIdentified[A](a: A)(implicit A: Identified[A]) =
    new IdentifiedOps[A](a)
}

final class IdentifiedOps[A](val a: A) extends AnyVal {
  def identity(implicit I: Identified[A]): I.Identity = I.identity(a)
}