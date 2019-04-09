package votelog.syntax

import votelog.infrastructure.encoding.Encoder

trait EncoderSyntax {
  implicit def syntaxEncoder[A](a: A): EncoderOps[A] = new EncoderOps[A](a)
}

final class EncoderOps[A](val a: A) extends AnyVal {
  def encodeAs[B](implicit E: Encoder[A, B]): Either[Throwable, B] = E.encode(a)
}