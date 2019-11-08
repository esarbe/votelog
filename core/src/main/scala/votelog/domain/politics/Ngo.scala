package votelog.domain.politics

import io.chrisdavenport.fuuid.FUUID

case class Ngo(name: String)

object Ngo {

  case class Id(value: FUUID)
}
