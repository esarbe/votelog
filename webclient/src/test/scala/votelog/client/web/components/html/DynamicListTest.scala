package votelog.client.web.components.html

import mhtml.Var
import org.scalajs.dom
import org.scalatest.{Matchers, WordSpec}

class DynamicListTest extends WordSpec with Matchers {

  val section = dom.document.createElement("section")
  val node = dom.document.appendChild(section)

  val list = Var(List(1,2,3,4))
  val render = (i: Int) => { <div> {i.toString} </div> }

  new DynamicList(list)


  "diff" should {
    "maintain state " in {
      list := List(1,2,3)
      node.childNodes.length shouldBe 3
    }
  }

}
