package votelog.persistence.doobie

import java.util.UUID

import cats.effect.{Async, ContextShift}
import doobie.util.transactor.Transactor
import votelog.app.{Configuration, Database}

object TransactorBuilder {

  def buildDatabaseConfig(name: String) =
    Database.Configuration(
      "org.h2.Driver",
      s"jdbc:h2:mem:${name};MODE=PostgreSQL;DB_CLOSE_DELAY=1",
      "sa",
      "",
    )

  def buildTransactor[F[_]: Async: ContextShift](name: String): Transactor[F] = {
    Database.buildTransactor(buildDatabaseConfig(name))
  }

  def buildTransactor[F[_]: Async: ContextShift]: Transactor[F] = {
    Database.buildTransactor(buildDatabaseConfig(UUID.randomUUID().toString))
  }
}
