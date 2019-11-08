package votelog.client.web.components

import mhtml.{Rx, Var}

class Signup(

) extends Component[Unit] {
  val username: Var[String] = Var("")
  val password: Var[String] = Var("")
  val submitSignup: Var[Unit] = Var(())



  val model = Rx(())
  val view = ???
}
