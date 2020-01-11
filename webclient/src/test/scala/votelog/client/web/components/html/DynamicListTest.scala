package votelog.client.web.components.html

import mhtml.{Rx, Var}
import org.scalajs.dom
import org.scalatest.{Matchers, WordSpec}

import scala.xml.Elem

class DynamicListTest extends WordSpec with Matchers {

  val section = dom.document.createElement("section")
  val node = dom.document.appendChild(section)

  val list: Var[List[Int]] = Var(List(1,2,3,4))
  val render: Int => Rx[Elem] = (i: Int) => Rx{ <div> {i.toString} </div> }

  DynamicList.mountOn(list, render)(node)

  "diff" should {
    "maintain state " in {
      list := List(1,2,3)
      node.childNodes.length shouldBe 3
    }
  }

}
