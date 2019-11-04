package votelog.client.web.components

import mhtml.Rx

import scala.xml.Node

trait Component[T] {
  def view: Node
  def state: Rx[T]
}
