package votelog.infrastructure

import cats.Functor
import endpoints.Tupler
import io.circe.KeyDecoder

trait Param[A] {
  def decode(params: Map[String, Iterable[String]]): Option[A]
}

object Param {

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
