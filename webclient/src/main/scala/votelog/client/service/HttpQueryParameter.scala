package votelog.client.service

/**
 * TODO: check if/how to reuse the Param infrastructure from votelog.webserver
  */
trait HttpQueryParameter[T] {
  def encode(t: T): String
}

object HttpQueryParameter {
  def apply[T](implicit ev: HttpQueryParameter[T]): ev.type = ev
}