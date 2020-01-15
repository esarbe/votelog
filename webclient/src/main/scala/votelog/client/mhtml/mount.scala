/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Olivier Blanvillain
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package votelog.client.mhtml

import mhtml.{Cancelable, Rx}
import org.scalajs.dom
import org.scalajs.dom.raw.{Node => DomNode}

import scala.scalajs.js
import scala.xml.{Node => XmlNode, _}

/** Side-effectly mounts an `xml.Node` to a `org.scalajs.dom.raw.Node`. */
object mount {

  case class Embeddable(e: Elem, f: DomNode => Cancelable)
  implicit val xmlElementEmbeddableEmbeddable: XmlElementEmbeddable[Embeddable] = null
  implicit val xmlElementEmbeddableNodeBuffer: XmlElementEmbeddable[Group] = null

  private val onMountAtt   = "mhtml-onmount"
  private val onUnmountAtt = "mhtml-onunmount"

  def apply(parent: DomNode, child: XmlNode): Cancelable = { mountNode(parent, child, None) }
  def apply(parent: DomNode, obs: Rx[XmlNode]): Cancelable = { mountNode(parent, new Atom(obs), None) }

  private def mountNode(parent: DomNode, child: XmlNode, startPoint: Option[DomNode]): Cancelable =
    child match {
      case e @ Elem(_, label, metadata, scope, child @ _*) =>
        val elemNode = e.namespace match {
          case Some(ns) => dom.document.createElementNS(ns, label)
          case None     => dom.document.createElement(label)
        }
        val cancelMetadata = metadata.map { m => mountMetadata(elemNode, scope, m, m.value) }
        val cancelChild = child.map(c => mountNode(elemNode, c, None))
        parent.mountHere(elemNode, startPoint)
        Cancelable { () => cancelMetadata.foreach(_.cancel); cancelChild.foreach(_.cancel) }

      case EntityRef(entityName)  =>
        val node = dom.document.createTextNode("").asInstanceOf[dom.Element]
        node.innerHTML = s"&$entityName;"
        parent.mountHere(node, startPoint)
        Cancelable.empty

      case Comment(text) =>
        parent.mountHere(dom.document.createComment(text), startPoint)
        Cancelable.empty

      case Group(nodes)  =>
        val cancels = nodes.map(n => mountNode(parent, n, startPoint))
        Cancelable(() => cancels.foreach(_.cancel))

      case a: Atom[_] => a.data match {
        case n: XmlNode  => mountNode(parent, n, startPoint)
        case rx: Rx[_]   =>
          val (start, end) = parent.createMountSection()
          var c1 = Cancelable.empty
          val c2 = rx.impure.run { v =>
            parent.cleanMountSection(start, end)
            c1.cancel
            c1 = mountNode(parent, new Atom(v), Some(start))
          }
          Cancelable { () => c1.cancel; c2.cancel }
        case Some(x)     => mountNode(parent, new Atom(x), startPoint)

        case Embeddable( e @ Elem(_, label, metadata, scope, child @ _*), f) =>
          val elemNode = e.namespace match {
            case Some(ns) => dom.document.createElementNS(ns, label)
            case None     => dom.document.createElement(label)
          }
          val cancelMetadata = metadata.map { m => mountMetadata(elemNode, scope, m, m.value) }
          val cancelChild = child.map(c => mountNode(elemNode, c, None))
          parent.mountHere(elemNode, startPoint)
          val c1 = f(elemNode)
          Cancelable { () => cancelMetadata.foreach(_.cancel); cancelChild.foreach(_.cancel); c1.cancel }

        case None        => Cancelable.empty
        case seq: Seq[_] => mountNode(parent, Group(seq.map(new Atom(_))), startPoint)
        case primitive   =>
          val content = primitive.toString
          if (!content.isEmpty)
            parent.mountHere(dom.document.createTextNode(content), startPoint)
          Cancelable.empty
      }
    }

  private def mountMetadata(parent: DomNode, scope: Option[Scope], m: MetaData, v: Any): Cancelable = v match {
    case a: Atom[_] =>
      mountMetadata(parent, scope, m, a.data)

    case Some(x: Any) =>
      mountMetadata(parent, scope, m, x)

    case r: Rx[_] =>
      val rx: Rx[_] = r
      var c1 = Cancelable.empty
      val c2 = rx.impure.run { value =>
        c1.cancel
        c1 = mountMetadata(parent, scope, m, value)
      }
      Cancelable { () => c1.cancel; c2.cancel }

    case f: Function0[Unit @ unchecked] =>
      if (m.key == onMountAtt) {
        f(); Cancelable.empty
      } else if(m.key == onUnmountAtt) {
        Cancelable(f)
      } else {
        parent.setEventListener(m.key, (_: dom.Event) => f())
      }

    case f: Function1[DomNode @ unchecked, Unit @ unchecked] =>
      if (m.key == onMountAtt) {
        f(parent); Cancelable.empty
      } else if (m.key == onUnmountAtt) {
        Cancelable(() => f(parent))
      } else {
        parent.setEventListener(m.key, f)
      }

    case _ =>
      parent.setMetadata(scope, m, v)
      Cancelable.empty
  }

  implicit class DomNodeExtra(node: DomNode) {
    def setEventListener[A](key: String, listener: A => Unit): Cancelable = {
      val dyn = node.asInstanceOf[js.Dynamic]
      dyn.updateDynamic(key)(listener)
      Cancelable(() => dyn.updateDynamic(key)(null))
    }

    def setMetadata(scope: Option[Scope], m: MetaData, v: Any): Unit = {
      val htmlNode = node.asInstanceOf[dom.html.Html]
      def set(key: String, prefix: Option[String]): Unit = v match {
        case null | None | false => htmlNode.removeAttribute(key)
        case _ =>
          val value = v match {
            case true => ""
            case _    => v.toString
          }
          if (key == "style") htmlNode.style.cssText = value
          else prefix.flatMap(p => scope.flatMap(_.namespaceURI(p))) match {
            case Some(ns) => htmlNode.setAttributeNS(ns, key, value)
            case None     => htmlNode.setAttribute(key, value)
          }
      }
      m match {
        case m: PrefixedAttribute[_] => set(s"${m.pre}:${m.key}", Some(m.pre))
        case _ => set(m.key, None)
      }
    }

    // Creats and inserts two empty text nodes into the DOM, which delimitate
    // a mounting region between them point. Because the DOM API only exposes
    // `.insertBefore` things are reversed: at the position of the `}`
    // character in our binding example, we insert the start point, and at `{`
    // goes the end.
    def createMountSection(): (DomNode, DomNode) = {
      val start = dom.document.createTextNode("")
      val end   = dom.document.createTextNode("")
      node.appendChild(end)
      node.appendChild(start)
      (start, end)
    }

    // Elements are then "inserted before" the start point, such that
    // inserting List(a, b) looks as follows: `}` → `a}` → `ab}`. Note that a
    // reference to the start point is sufficient here. */
    def mountHere(child: DomNode, start: Option[DomNode]): Unit =
    { start.fold(node.appendChild(child))(point => node.insertBefore(child, point)); () }

    // Cleaning stuff is equally simple, `cleanMountSection` takes a references
    // to start and end point, and (tail recursively) deletes nodes at the
    // left of the start point until it reaches end of the mounting section. */
    def cleanMountSection(start: DomNode, end: DomNode): Unit = {
      val next = start.previousSibling
      if (next != end) {
        node.removeChild(next)
        cleanMountSection(start, end)
      }
    }
  }
}
