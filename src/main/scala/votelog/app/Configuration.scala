package votelog.app

case class Configuration(
  http: Configuration.Http,
  security: Configuration.Security,
  database: Database.Configuration
)

object Configuration {
  case class Http(port: Int, interface: String)
  case class Security(passwordSalt: String, secret: String)
}
