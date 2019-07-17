package votelog.app

case class Configuration(
  security: Configuration.Security,
  database: Database.Configuration
)

object Configuration {
  case class Security(passwordSalt: String, secret: String)
}
