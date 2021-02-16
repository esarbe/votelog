package votelog.persistence

import cats.effect.Bracket
import _root_.doobie.Fragment
import _root_.doobie.implicits.toSqlInterpolator

import scala.collection.immutable

package object doobie {
  type ThrowableBracket[F[_]] = Bracket[F, Throwable]

  def buildOrderBy(orderings: immutable.Seq[String]): Fragment = {
      if (orderings.nonEmpty) fr"ORDER BY ${mkFrag(orderings,",")}"
      else Fragment.empty
  }

  def buildFields(fields: immutable.Seq[String]) = mkFrag(fields, "", ",", "")
  def buildSubsequentFields(fields: immutable.Seq[String]): Fragment =  mkFrag(fields, ",", ",", "")

  def mkFrag(ts: immutable.Seq[String], sep: String): Fragment = mkFrag(ts, "", sep, "")
  def mkFrag(ts: immutable.Seq[String], start: String, sep: String, end: String): Fragment = {
    var init = Fragment.empty
    if (start.nonEmpty) init = init ++ Fragment.const(start)
    val it = ts.iterator
    if (it.hasNext) {
      init = init ++ Fragment.const(it.next())
      while (it.hasNext) {
        init = init ++ Fragment.const(s"$sep${it.next()}")
      }
    }
    if (end.nonEmpty) init = init ++ Fragment.const(end)
    init
  }
}
