package votelog.service

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.wordspec.AnyWordSpec
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.param
import votelog.domain.politics.Person.{Gender, Name}
import votelog.domain.politics.{Business, Canton, Context, Language, Person, VoteAlg, Votum}
import votelog.infrastructure.StoreService
import votelog.persistence.PersonStore

class PersonServiceTest extends AnyFlatSpec {

  val person = Person(Person.Id(1), Name("foo"), Name("bar"), Canton("ZH"), Gender.Female, "", None, None)
  val vote = new VoteAlg[IO] {
    override def getVotesForBusiness(context: Context)
      (business: Business.Id): IO[List[(Person.Id, Votum)]] = IO.pure(Nil)

    override def getVotesForPerson(context: Context)
      (person: Person.Id): IO[List[(Business.Id, Votum)]] = IO.pure(Nil)
  }

  val personStore = new PersonStore[IO] {
    override def index(
      queryParameters: ReadOnlyStoreAlg.IndexQueryParameters[Context]
    ): IO[Index[Person.Id]] = IO.pure(Index(0, List(person.id)))

    override def read(queryParameters: Language)
      (id: Person.Id): IO[Person] = IO.pure(person)
  }

  //new PersonService(Component("/"), vote, )


  it should "decode query parameters" in {

    val indexQueryParamDecoder: param.Decoder[IndexQueryParameters[Context]] =
      iqpc => Params.indexQueryParam(Params.contextParam).decode(iqpc)

    val qp = "?lang=en&lp=50&os=0&ps=10"

    //indexQueryParamDecoder.decode(qp)


  }


}
