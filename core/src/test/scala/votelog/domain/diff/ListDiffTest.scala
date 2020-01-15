package votelog.domain.diff

import org.scalatest.{Matchers, WordSpec}
import votelog.domain.diff.ListDiff.Op
import votelog.domain.diff.ListDiff.Op.{Delete, Insert}

import scala.collection.mutable.ArrayBuffer

class ListDiffTest extends WordSpec with Matchers {
  "diff" should {
    def update(prev: List[Int], curr: List[Int], changes: Seq[Op]): List[Int] = {
      val buffer = ArrayBuffer[Int](prev:_*)

      var i = 0
      changes.foreach {
        case Delete(beforeIndex) =>
          buffer.remove(beforeIndex - i)
          i += 1
        case Insert(indexPrev, indexCurr) =>
          buffer.insert(indexPrev - i, curr(indexCurr))
          i -= 1
      }

      buffer.toList
    }

    "find an empty edit list" in {
      ListDiff.diff(Nil, List(1)) shouldBe Vector(Insert(0,0))
    }

    "find a edit sequence" in {
      ListDiff.diff(List(1), List(2)) shouldBe Vector(Op.Delete(0), Insert(1,0))
    }

    "be able to reconstruct a list" in {
      val prev = List(1,2,3,4)
      val curr = List(3,4,5,6)
      val expected = List(Delete(0), Delete(1), Insert(4,2), Insert(4, 3))
      ListDiff.diff(prev, curr) shouldBe expected

      update(prev, curr, expected) shouldBe curr
    }

    "be able to create a list" in {
      val prev = List()
      val curr = List(1,2,3,4)
      val expected = List(Insert(0,0), Insert(0,1), Insert(0,2), Insert(0,3))
      ListDiff.diff(prev, curr) shouldBe expected
      update(prev, curr, expected) shouldBe curr
    }
  }
}
