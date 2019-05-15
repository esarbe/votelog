package votelog.app

case class Configuration(
  http: Configuration.Http,
  security: Configuration.Security,
  database: Configuration.Database
)

object Configuration {
  case class Http(port: Int, interface: String)
  case class Security(passwordSalt: String, secret: String)
  case class Database(url: String, user: String, password: String)
}