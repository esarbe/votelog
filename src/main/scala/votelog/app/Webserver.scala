package votelog.app

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.h2._
import votelog.app.Webserver.transactor
import votelog.implementation.Log4SLogger
import votelog.persistence.doobie.DoobiePoliticianTable

object Webserver extends IOApp {
  val log = new Log4SLogger[IO](org.log4s.getLogger)

  val pt = new DoobiePoliticianTable


  val transactor: Resource[IO, H2Transactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]    // our transaction EC
      xa <- H2Transactor.newH2Transactor[IO](
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
        "sa",                                   // username
        "",                                     // password
        ce,                                     // await connection here
        te                                      // execute JDBC operations here
      )
    } yield xa


  def run(args: List[String]): IO[ExitCode] =
    transactor.use { xa =>
      for {
        _ <- log.info("Deleting and re-creating database")
      } yield ExitCode.Success
    }
}
