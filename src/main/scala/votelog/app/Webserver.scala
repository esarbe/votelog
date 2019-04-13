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
import votelog.domain.model.{Motion, Politician, Votum}
import votelog.implementation.Log4SLogger
import votelog.infrastructure.StoreAlg
import votelog.persistence.{MotionStore, PoliticianStore}
import votelog.persistence.doobie.{DoobieSchema, DoobieVoteStore}
import votelog.service.{DoobieMotionStore, DoobiePoliticianStore, MotionStoreService, PoliticianService}

import scala.util.Try

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

      val ms: StoreAlg[IO, Motion, Motion.Id, MotionStore.Recipe] =
        new DoobieMotionStore[IO] { val transactor: H2Transactor[IO] = xa }

      val init: IO[ExitCode] =
        for {
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
              .map(_.flatMap(p => log.info(s"found politician '$p'")))
              .getOrElse(log.warn("unable to find any politician"))
          //_ <- ps.delete(Politician.Id(4))
          _ <- ms.create(MotionStore.Recipe("eat the rich 2", Politician.Id(1)))
          // motions
          motions <- ms.index.flatMap(_.map(ms.read).sequence)
          _ <- motions.map(m => log.info(s"found motion: $m")).sequence
          _ <- vs.voteFor(Politician.Id(1), Motion.Id(1), Votum.Yes)
          _ <- log.info("end of run")
        } yield ExitCode.Success

      val q: IO[Unit] =
        init.attempt.flatMap {
          case Right(a) => log.info("all went well")
          case Left(error) => log.info(s"something went wrong: $error")
        }

      q *> {

        val pws = new PoliticianService(ps, vs, log)
        val mws = new MotionStoreService(ms)

        val httpRoutes: HttpRoutes[IO] =
          Router(
            "/api/politician" -> pws.service,
            "/api/motion" -> mws.service
          )


        val server = for {
          port <- IO(sys.env("PORT"))
          _ <- log.info(s"attempting to bind to port $port")
          server <-
            BlazeServerBuilder[IO]
              .bindHttp(port.toInt)
              .withHttpApp(httpRoutes.orNotFound)
              .serve
              .compile
              .drain

        } yield server

        
        server.as(ExitCode.Success)
      }
    }

}
