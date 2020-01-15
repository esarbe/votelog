package votelog.client.web.components.html

import mhtml.{Rx, Var}

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.xml.Elem

object tools {

  def ifEnter(run: js.Dynamic => Unit): js.Dynamic => Unit = {
    event =>
      if (event.keyCode == 13) run(event)
  }

  def input(ofType: String)(id: String, label: String, rx: Var[String], errors: Rx[List[(String, String)]]) = {
    val filteredErrors: Rx[List[String]] =
      errors.map(_.toMap.filterKeys(_ == id).values.toList).dropRepeats

    <dl>
      <dt><label for={id}>{label}</label></dt>
      <dd><input type={ofType} id={id} value={rx} onchange={set(rx)}/></dd>
      <dd>{ filteredErrors.map { _.map { error => <error>{error}</error> }}} </dd>
    </dl>
  }

  def inputText(id: String, label: String, rx: Var[String], errors: Rx[List[(String, String)]]): Elem =
    input("text")(id, label, rx, errors)

  def inputPassword(id: String, label: String, rx: Var[String], errors: Rx[List[(String, String)]]): Elem =
    input("password")(id, label, rx, errors)

  object debounce {
    var timeoutHandler: js.UndefOr[SetTimeoutHandle] = js.undefined
    def apply[A, B](timeout: Double)(f: A => B): A => Unit = { a =>
      timeoutHandler foreach js.timers.clearTimeout
      timeoutHandler = js.timers.setTimeout(timeout) {
        f(a)
        ()
      }
    }
  }

  def setTo[T](t: T, vrx: Var[T]): js.Dynamic => Unit  = {
    _ => vrx := t
  }

  def update[T](vrx: Var[T])(f: T => T): js.Dynamic => Unit = {
    _ => vrx.update(f)
  }

  def trigger(vrx: Var[Unit]): js.Dynamic => Unit = {
    _ => vrx := Unit
  }

  def set[T](vrx: Var[T]): js.Dynamic => Unit = {
    event =>
      vrx := event.target.value.asInstanceOf[T]
  }
}
