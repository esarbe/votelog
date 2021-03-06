package votelog.client.web.components.html

import cats._
import cats.implicits._
import mhtml.{Rx, Var}

import scala.scalajs.js
import scala.xml.{Elem, Node}

/**
  * an html select element whose options might change
  */
class DynamicSelect[T: Show](legend: String, options: Rx[Set[T]], default: T)  {

  val model: Var[T] = Var[T](default)

  private def set(indexedOptions: Map[Int, T])(value: Var[T]): js.Dynamic => Unit = {
    event =>
      value := indexedOptions(event.target.value.asInstanceOf[String].toInt)
  }

  private def option(selected: Rx[T])(option: T) = {
     <option selected={selected.map(_ == option)} value={option.hashCode.toString}>{option.show}</option>
  }

  def view: Node =
    <fieldset>{
      <legend>{legend}</legend>
      options
        .map { options =>
          val indexedOptions = (options.map(_.hashCode) zip options).toMap
          <select onchange={set(indexedOptions)(model)}>{
            options.map(option(model)).toList
          }</select>
        }
    }</fieldset>
}