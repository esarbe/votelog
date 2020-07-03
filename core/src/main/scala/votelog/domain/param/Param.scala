package votelog.domain.param

import cats.Functor
import io.circe.KeyDecoder
import votelog.Tupler

/**
 * Allows to encode structured data in url query parameters and
 * parse structured data from url query parameters
 *
 * WARNING: this is very simple code that does not try to
 * mitigate programmer errors like duplicated keys.
 */
trait Param[T] {
  def decode(params: Map[String, Iterable[String]]): Option[T]
  def encode(t: T): String
}

object Param {

  implicit class ParamOps[T](t: T)(implicit ev: Param[T]) {
    def urlEncode: String = ev.encode(t)
  }

  def always[T](value: T): Param[T]  = new Param[T] {
    def decode(params: Map[String, Iterable[String]]) = Some(value)
    def encode
  }

  implicit object paramFunctor extends Functor[Param] {
    override def map[A, B](param: Param[A])(f: A => B): Param[B] =
      params => param.decode(params).map(f)
  }

  def apply[T: KeyDecoder](name: String): Param[T] =
    params => params.get(name).flatMap(vs => KeyDecoder[T].apply(vs.head))

  def combine[A, B](a: Param[A], b: Param[B])(implicit ev: Tupler[A, B]): Param[ev.Out] = {
    params: Map[String, Iterable[String]] =>
      for {
        a <- a.decode(params)
        b <- b.decode(params)
      } yield ev.apply(a, b)
  }
}
