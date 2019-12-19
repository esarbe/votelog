package votelog.client.web.components

import mhtml.{Rx, Var}
import html.tools._

import scala.scalajs.js

class Signup() {
  val username: Var[String] = Var("")
  val password: Var[String] = Var("")
  val submitSignup: Var[Unit] = Var(())

  def ifEnter(run: js.Dynamic => Unit): js.Dynamic => Unit = {
    event =>
      if (event.keyCode == 13) run(event)
  }

  val view = {
    <fieldset onkeyup={ ifEnter(set(submitSignup)) }>
      <legend>Login</legend>
      <dl>
        <dt>
          <label for="username">Username</label>
        </dt>
        <dd>
          <input id="username" type="text" value={ username }  oninput={ debounce(200)(set(username)) } />
        </dd>
      </dl>
      <dl>
        <dt>
          <label for="password">Password</label>
        </dt>
        <dd>
          <input id="password" type="password" value={ password } oninput={ debounce(200)(set(password)) } />
        </dd>
      </dl>

      <input type="button" value="login" onclick={ set(submitSignup) } />

    </fieldset>
  }


}
