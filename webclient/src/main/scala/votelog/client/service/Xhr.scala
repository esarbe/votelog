package votelog.client.service

trait Xhr {
  // this should be part of the param encoding but we rely on js here
  def urlEncodeParams(s: String) =
    "?" + s.drop(1)
      .split('&')
      .map(scala.scalajs.js.URIUtils.encodeURI)
      .mkString("&")

}
