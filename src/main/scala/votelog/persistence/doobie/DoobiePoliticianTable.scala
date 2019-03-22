package votelog.persistence.doobie

import votelog.persistence.politician.PoliticianTable
import doobie._
import doobie.implicits._
import cats._
import cats.implicits._

class DoobiePoliticianTable extends PoliticianTable[ConnectionIO] {
  override def inititalize: ConnectionIO[Unit] = {
    val drop =
      sql"""
            DROP TABLE IF EXISTS politician
        """.update.run

    val create =
      sql"""
            CREATE TABLE person (
              id   SERIAL AUTO_INCREMENT,
              name VARCHAR NOT NULL UNIQUE
            )
        """.update.run

    drop *> create.map(_ => Unit)
  }
}
