package votelog.persistence.doobie

import cats.effect.{Async, ContextShift}
import doobie.util.transactor.Transactor
import votelog.app.{Configuration, VoteLog}

object TransactorBuilder {

  def buildDatabaseConfig(name: String) =
    Configuration.Database(
      Configuration.Database.Driver.H2,
      s"jdbc:h2:mem:${getClass.getName};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "sa",
      "",
    )

  def buildTransactor[F[_]: Async: ContextShift](name: String): Transactor[F] = {
    VoteLog.connectToDatabase(buildDatabaseConfig(name))
  }
}
