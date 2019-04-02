package votelog.app

import votelog.implicits._
import cats.MonadError
import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.h2._
import io.circe
import io.circe.Decoder
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, HttpApp, HttpRoutes}
import votelog.app.Webserver.transactor
import votelog.domain.model.Motion.MotionIdentified
import votelog.domain.model.{Politician, Politician2}
import votelog.domain.service.PoliticianService
import votelog.implementation.Log4SLogger
import votelog.infrastructure.encoding.Encoder
import votelog.infrastructure.logging.Logger
import votelog.infrastructure.{CrudService, Identified, RestService}
import votelog.persistence.doobie.{DoobiePoliticianRepository, DoobiePoliticianTable}
import votelog.domain.service.PoliticianService.Recipe
import org.http4s.server.blaze.{BlazeBuilder, BlazeServerBuilder}

object Webserver extends IOApp {
  val log = new Log4SLogger[IO](org.log4s.getLogger)

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
    transactor.use { xa: H2Transactor[IO] =>

      val pt = new DoobiePoliticianTable
      val pc = new DoobiePoliticianRepository { val transactor: H2Transactor[IO] = xa }
      val ps = new votelog.service.PoliticianServiceImpl(pc)
      val pws =
        new RestService[Politician] {

          import io.circe.generic.semiauto._

          val crud = ps
          val Mount = "politician"
          override implicit val Log: Logger[IO] = log
          override implicit val IdEncoder: Encoder[String, Politician.Id] = PoliticianIdFromStringDecoder
          override implicit val tidEncoder: circe.Encoder[Politician.Id] = deriveEncoder[Politician.Id]
          implicit val tidDecoder: circe.Decoder[Politician.Id] = deriveDecoder[Politician.Id]
          override implicit val recipeDecoder: circe.Decoder[Recipe] = deriveDecoder[Recipe]
          override implicit val tEncoder: circe.Encoder[Politician] = deriveEncoder[Politician]
          override implicit val tDecoder: circe.Decoder[Politician] = deriveDecoder[Politician]
        }

      val init = for {
        _ <- log.info("Deleting and re-creating database")
        _ <- pt.inititalize.transact(xa)
        _ <- log.info("Deleting and re-creating database successful")
        fooId <- ps.create(Recipe("foo"))
        barId <- ps.create(Recipe("bar"))
        _ <- log.info(s"foo has id '$fooId'")
        _ <- log.info(s"bar has id '$barId'")
        bar <- ps.read(fooId)
        ids <- pc.index
        _ <- ids.map(id => log.info(id.toString)).sequence
        _ <-
          ids
            .headOption
            .map(pc.read)
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

        import org.http4s.server.blaze._
        // import org.http4s.server.blaze._

        import org.http4s.implicits._
        val httpRoutes: HttpRoutes[IO] = Router("/api" -> pws.service)

        val foo = httpRoutes.orNotFound

        val server = BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(foo)

        server.serve.compile.drain.as(state)
      }
    }



}
