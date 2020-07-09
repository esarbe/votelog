package votelog.domain.param

import cats._
import cats.implicits._
import io.circe.KeyDecoder
import votelog.Tupler

/**
 * Allows to encode structured data in url query parameters and
 * parse structured data from url query parameters
 *
 * WARNING: this is very simple code that does not try to
 * mitigate programmer errors like duplicated keys.
 */
case class Param[T](label: String, key: String, description: String)
case class Params(entries: Map[String, Iterable[String]]) {
  def urlEncode: String =
    entries.mapValues(_.mkString(",")).toList.map { case (key, values) => s"$key=$values"}.mkString("?", ";", "")
}

object Params {

  def empty: Params = ParamsMonoid.empty

  implicit object ParamsMonoid extends Monoid[Params] {
    override def empty: Params = Params(Map.empty[String, Seq[String]])

    override def combine(x: Params, y: Params): Params = Params(x.entries |+| y.entries)
  }
}

trait Encoder[T] {
  def encode(t: T): Params
}

object Encoder {
  implicit final class ParamDecoderOps[T](t: T)(implicit ev: Encoder[T]) {
    def urlEncode: String = ev.encode(t).urlEncode
  }

  def unit[T]: Encoder[T] = _ => Params.empty
}


trait Decoder[T] {
  def decode(p: Params): Option[T]
}

object Decoder {

  implicit class DecoderOps[T: Param](t: T)(implicit ev: Encoder[T]) {
    def urlEncode: String = ev.encode(t).urlEncode
  }

  def always[T](value: T): Decoder[T]  = (params: Params) => Some(value)

  implicit object decoderFunctor extends Functor[Decoder] {
    override def map[A, B](decoder: Decoder[A])(f: A => B): Decoder[B] =
      params => decoder.decode(params).map(f)
  }

  def apply[T: KeyDecoder](key: String): Decoder[T] =
    params => params.entries.get(key).flatMap(vs => KeyDecoder[T].apply(vs.head))

  def combine[A, B](a: Decoder[A], b: Decoder[B])(implicit ev: Tupler[A, B]): Decoder[ev.Out] = {
    params: Params =>
      for {
        a <- a.decode(params)
        b <- b.decode(params)
      } yield ev.apply(a, b)
  }
}
