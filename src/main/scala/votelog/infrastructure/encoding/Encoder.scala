package votelog.infrastructure.encoding

trait Encoder[A, B] {
  def encode(a: A): Either[Throwable, B]
}
