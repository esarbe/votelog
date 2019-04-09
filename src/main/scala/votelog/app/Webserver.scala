package votelog.app

import cats.Monad
import cats.effect._
import cats.implicits._
import doobie._
import doobie.h2._
import doobie.implicits._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import votelog.domain.model.Politician
import votelog.implementation.Log4SLogger
import votelog.persistence.PoliticianStore
import votelog.persistence.doobie.{DoobieSchema, DoobieVoteStore}
import votelog.service.{DoobiePoliticianStore, PoliticianService}

object Webserver extends IOApp {
  val log = new Log4SLogger[IO](org.log4s.getLogger)

  val transactor: Resource[IO, H2Transactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]    // our transaction EC
      xa <- H2Transactor.newH2Transactor[IO](
        "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", // connect URL
        "sa",                                   // username
        "",                                     // password
        ce,                                     // await connection here
        te                                      // execute JDBC operations here
      )
    } yield xa


  def run(args: List[String]): IO[ExitCode] =
    transactor.use { xa: H2Transactor[IO] =>

      val pt: DoobieSchema = new DoobieSchema
      val ps = new DoobiePoliticianStore[IO] { val transactor: H2Transactor[IO] = xa }
      val vs =
        new DoobieVoteStore[IO] {
          val transactor: H2Transactor[IO] = xa
          val fMonad: Monad[IO] = implicitly[Monad[IO]]
        }

      val pws = new PoliticianService(ps, vs, log)

      val init = for {
          _ <- log.info("Deleting and re-creating database")
          _ <- pt.initialize.transact(xa)
          _ <- log.info("Deleting and re-creating database successful")
          fooId <- ps.create(PoliticianStore.Recipe("foo"))
          barId <- ps.create(PoliticianStore.Recipe("bar"))
          _ <- log.info(s"foo has id '$fooId'")
          _ <- log.info(s"bar has id '$barId'")
          bar <- ps.read(fooId)
          ids <- ps.index
          _ <- ids.map(id => log.info(id.toString)).sequence
          _ <-
            ids
              .headOption
              .map(ps.read)
              .map(_.flatMap(p => log.info(s"found politician '${p}'")))
              .getOrElse(log.warn("unable to find any politician"))
          _ <- ps.delete(Politician.Id(4))
          _ <-
            ps.read(Politician.Id(3)).attempt.flatMap {
              case Right(p) => log.info(s"found politician: $p")
              case Left(error) => log.info(s"politician 3 not found: $error")
             }
          _ <- log.info("end of run")


        } yield ExitCode.Success

      init.flatMap { state: ExitCode =>

        val httpRoutes: HttpRoutes[IO] = Router("/api" -> pws.service)

        val port = sys.env("PORT")

        val server =
          BlazeServerBuilder[IO]
            .bindHttp(port.toInt)
            .withHttpApp(httpRoutes.orNotFound)

        server.serve.compile.drain.as(state)
      }
    }

}
