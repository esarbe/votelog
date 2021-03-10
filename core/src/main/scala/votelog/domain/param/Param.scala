package votelog.domain.param

import cats._
import cats.implicits._
import io.circe.KeyDecoder

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
    entries.view.mapValues(_.mkString(",")).toList.map { case (key, values) => s"$key=$values"}.mkString("?", "&", "")
}

object Params {

  implicit object ParamsMonoid extends Monoid[Params] {
    def empty: Params = Params(Map.empty[String, Seq[String]])
    def combine(x: Params, y: Params): Params = Params(x.entries <+> y.entries)
  }
}

trait Encoder[T] {
  def encode(t: T): Params
}

object Encoder {
  implicit final class ParamEncoderOps[T](t: T)(implicit ev: Encoder[T]) {
    def urlEncode: String = ev.encode(t).urlEncode
  }

  def unit[T]: Encoder[T] = _ => Monoid[Params].empty
}


trait Decoder[A] {
  def decode(p: Params): Option[A]
  def zip[B](other: Decoder[B]): Decoder[(A, B)] = Decoder.combineDecoder(this, other)
}

object Decoder {

  implicit class Ops[A](val decoder: Decoder[A]) extends AnyVal {
    def decode(params: Params): Option[A] = decoder.decode(params)
    def zip [B](other: Decoder[B]): Decoder[(A, B)] = combineDecoder[A, B](decoder, other)
  }

  implicit def listKeyDecoder[A](implicit ev: KeyDecoder[A]): KeyDecoder[List[A]] =
    new KeyDecoder[List[A]] {
      def apply(key: String): Option[List[A]] =
        key.split(',').map(ev.apply).toList.sequence
    }

  def always[T](value: T): Decoder[T] =
    new Decoder[T]{
      override def decode(p: Params): Option[T] = Some(value)
    }

  implicit object decoderFunctor extends Functor[Decoder] {
    override def map[A, B](decoder: Decoder[A])(f: A => B): Decoder[B] =
      params => decoder.decode(params).map(f)
  }

  def apply[T: KeyDecoder](key: String): Decoder[T] =
    params => params.entries.get(key).flatMap(vs => KeyDecoder[T].apply(vs.head))

  def combineDecoder[A, B](a: Decoder[A], b: Decoder[B]): Decoder[(A, B)] = {
    params: Params =>
      for {
        a <- a.decode(params)
        b <- b.decode(params)
      } yield (a, b)
  }
}
