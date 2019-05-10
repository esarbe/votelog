package votelog.infrastructure.encoding

/** this can be replaced with [[io.circe.KeyEncoder]]
  */
trait Encoder[A, B] {
  def encode(a: A): Either[Throwable, B]
}