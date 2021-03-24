package votelog.service

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction.Ascending
import votelog.domain.param.Params
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, PersonPartial, VoteAlg, Votum}
import votelog.infrastructure.logging.Logger
import votelog.persistence.PersonStore

class PersonServiceSpec extends AnyFlatSpec with Matchers {

  val component = Component("test")
  val store = new PersonStore[IO] {
    override def index(queryParameters: IndexQueryParameters[Context, Person.Field, Person.Field]): IO[ReadOnlyStoreAlg.Index[Person.Id, PersonPartial]] = ???
    override def read(queryParameters: Language)(id: Person.Id): IO[Person] = ???
  }

  val voteAlg = new VoteAlg[IO] {
    override def getVotesForBusiness(context: Context)(business: Business.Id): IO[List[(Person.Id, Votum)]] = ???
    override def getVotesForPerson(context: Context)(person: Person.Id): IO[List[(Business.Id, Votum)]] = ???
  }

  val auth: AuthorizationAlg[IO] = (_, _, _) => IO.pure(true)
  val log = new Logger[IO] {
    override def warn(message: String): IO[Unit] = ???
    override def info(message: String): IO[Unit] = ???
    override def error(t: Throwable)(message: String): IO[Unit] = ???
    override def error(message: String): IO[Unit] = ???
    override def debug(t: Throwable)(message: String): IO[Unit] = ???
    override def debug(message: String): IO[Unit] = ???
  }

  val service = new PersonService(component, store, voteAlg, log, auth)

  "PersonService" should "" in {
    val urlParam = "?lp=50&os=0&ps=10&fields=FirstName,LastName&orderBy=LastName|Asc,FirstName|Asc&lang=de"


    val ps = urlParam.drop(1).split('&').map { p =>
      val key :: value :: Nil = p.split('=').toList
      key -> List(value)
    }.toMap

    service.indexQueryParamDecoder.decode(Params(ps)) shouldBe
      Some(
        IndexQueryParameters(
          PageSize(10),
          Offset(0),
          Context(LegislativePeriod.Id(50), Language.German),
          List(
            Person.Field.LastName -> Ascending,
            Person.Field.FirstName -> Ascending),
          Set(Person.Field.FirstName, Person.Field.LastName)
        )
      )
  }

}
