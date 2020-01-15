package votelog.client.web.components.html

import cats._
import cats.implicits._
import mhtml.{Rx, Var}
import votelog.client.web.components.id

import scala.scalajs.js
import scala.xml.{Elem, Node}

/**
  * an html select element whose options won't change
  */
class StaticSelect[T: Show : Ordering](
  legend: String,
  options: Seq[T],
  default: T,
  clazz: String,
  id: String
) {

  val model = Var[T](default)

  private def set(indexedOptions: Map[Int, T])(value: Var[T]): js.Dynamic => Unit = {
    event =>
      value := indexedOptions(event.target.value.asInstanceOf[String].toInt)
  }

  private def option(selected: Rx[T])(option: T): Elem = {
    <option selected={selected.map(_ == option)} value={option.hashCode.toString}>{option.show}</option>
  }

  def view: Node = {
    val indexedOptions = (options.sorted.map(_.hashCode) zip options).toMap

    <dl class={clazz}>
      <dt><label for={id} >Page Size</label></dt>
      <dd>
        <select id={id} onchange={ set(indexedOptions)(model) }>
          { options.map(option(model)).toList }
        </select>
      </dd>
    </dl>
  }
}
