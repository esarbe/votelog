package votelog.client.web

import org.scalajs.dom
import scalatags.Text
import scalatags.Text.all._

object Application {
  def main(args: Array[String]): Unit = {


    println("hello, worlds!")
  }


  def app(content: Text.Modifier) = {
    body(
      tag("section").apply(content)
    )
  }
}
