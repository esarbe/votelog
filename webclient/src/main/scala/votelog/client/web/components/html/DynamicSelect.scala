package votelog.client.web.components.html

import cats._
import cats.implicits._
import mhtml.{Rx, Var}
import votelog.client.web.components.Component

import scala.scalajs.js
import scala.xml.{Elem, Node}

class Select[T: Show](legend: String, options: Rx[Set[T]], default: T) extends Component[T] {

  val model = Var[T](default)

  def set(indexedOptions: Map[Int, T])(value: Var[T]): js.Dynamic => Unit = {
    event =>
      value := indexedOptions(event.target.value.asInstanceOf[String].toInt)
  }

  def option(selected: Rx[T])(option: T) = {
     <option selected={selected.map(_ == option)} value={option.hashCode.toString}>{option.show}</option>

  }

  override def view: Node =
    <fieldset>{
      options
        .map { options =>
          val indexedOptions = (options.map(_.hashCode) zip options).toMap
          <select onchange={set(indexedOptions)(model)}>{
            options.map(option(model)).toList
          }</select>
        }
    }</fieldset>
}