package votelog.client.web.components.html

import mhtml.Var

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

object tools {

  def ifEnter(run: js.Dynamic => Unit): js.Dynamic => Unit = {
    event =>
      if (event.keyCode == 13) run(event)
  }

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

  def set[T](value: Var[T]): js.Dynamic => Unit = {
    event =>
      value.update(_ => event.target.value.asInstanceOf[T])
  }
}
