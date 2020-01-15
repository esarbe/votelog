package votelog.client.web

import votelog.domain.authorization.Component

package object components {
  def id(id: String)(implicit ev: Component): String = ev.child(id).location
}
