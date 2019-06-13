package votelog.persistence.doobie

import cats.effect.{Async, ContextShift}
import doobie.util.transactor.Transactor
import votelog.app.{Configuration, Database}

object TransactorBuilder {

  def buildDatabaseConfig =
    Configuration.Database(
      "org.h2.Driver",
      s"jdbc:h2:./test.db;MODE=PostgreSQL;DB_CLOSE_DELAY=1",
      "sa",
      "",
    )

  def buildDatabaseConfig(name: String) =
    Configuration.Database(
      "org.h2.Driver",
      s"jdbc:h2:mem:${name};MODE=PostgreSQL;DB_CLOSE_DELAY=1",
      "sa",
      "",
    )


  def buildTransactor[F[_]: Async: ContextShift](name: String): Transactor[F] = {
    Database.buildTransactor(buildDatabaseConfig(name))
  }
}
