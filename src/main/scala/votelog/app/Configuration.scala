package votelog.app

case class Configuration(
  http: Configuration.Http,
  security: Configuration.Security,
  database: Configuration.Database
)

object Configuration {
  case class Http(port: Int, interface: String)
  case class Security(passwordSalt: String, secret: String)
  case class Database(driver: Database.Driver, url: String, user: String, password: String)

  object Database {
    sealed trait Driver { val name: String }

    object Driver {
      case object H2 extends Driver { val name: String = "org.h2.Driver"}
      case object Postgres extends Driver { val name: String = "org.postgresql.Driver"}
    }
  }
}
