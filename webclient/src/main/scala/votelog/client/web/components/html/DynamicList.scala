package votelog.client.web.components.html

import mhtml.{Cancelable, Rx}
import org.scalajs.dom
import org.scalajs.dom.{Element, Node => DomNode}
import votelog.domain.diff.ListDiff
import votelog.domain.diff.ListDiff.Op.{Delete, Insert}

import scala.collection.mutable.ArrayBuffer
import scala.xml.Node

object DynamicList {

  def mountOn[T](rx: Rx[List[T]], render: T => Rx[Node])(listNode: DomNode): Cancelable = {
    val elementNodes = ArrayBuffer.empty[(DomNode, Cancelable)]

    val seed = (Seq.empty[ListDiff.Op], List.empty[T])
    val changesAndState: Rx[(Seq[ListDiff.Op], List[T])] =
      rx.foldp(seed){ case ((_, last), current) =>
        (ListDiff.diff(last, current), current)
      }

    val c1 = changesAndState.impure.run { case (diff, state) =>
      runDomChanges(listNode, elementNodes, diff, state, render)
    }

    Cancelable { () => c1.cancel; elementNodes.foreach(_._2.cancel) }
  }


  def runDomChanges[T](
    parent: DomNode,
    buffer: ArrayBuffer[(DomNode, Cancelable)],
    changes: Seq[ListDiff.Op],
    currentValues: List[T],
    render: T => Rx[Node],
  ): Unit = {
    var indexOffset = 0
    changes.foreach {
      case Delete(position) =>
        val (node, callback) = buffer(position - indexOffset)
        callback.cancel
        parent.removeChild(node)
        buffer.remove(position - indexOffset)
        indexOffset += 1

      case Insert(indexPrev, indexCurr) =>
        val node = createElement(parent, indexPrev - indexOffset, buffer.map(_._1))
        val rx = render(currentValues(indexCurr))
        val callback = mhtml.mount(node, rx)
        buffer.insert(indexPrev - indexOffset, (node, callback))
        indexOffset -= 1
    }
  }

  def createElement(parent: DomNode, insertBeforeIndex: Int, siblings: ArrayBuffer[DomNode]): dom.raw.Node = {
    val li: dom.raw.Node = dom.document.createElement("li")
    if (insertBeforeIndex >= siblings.size) {
      parent.appendChild(li)
    } else {
      val insertBeforeSibling = siblings(insertBeforeIndex)
      parent.insertBefore(li, insertBeforeSibling)
    }
    li
  }

  def createList(parent: DomNode): Element = {
    val ul = dom.document.createElement("ul")
    parent.appendChild(ul)
    ul
  }
}