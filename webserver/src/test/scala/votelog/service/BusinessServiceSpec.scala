package votelog.service

import cats.Id
import cats.effect.IO
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{AuthedRequest, EntityDecoder, Method, Request, Response, Status, Uri}
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import votelog.orphans.circe.implicits._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, VoteAlg, Votum}
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters, QueryParameters}
import votelog.domain.data.Sorting
import votelog.domain.data.Sorting.Direction.Descending
import votelog.domain.param.Params
import votelog.persistence.BusinessStore
import votelog.service.BusinessServiceSpec.check

class BusinessServiceSpec extends AnyFlatSpec with Matchers {

  val store =
    new BusinessStore[IO] {
      override def index(queryParameters: IndexParameters): IO[Index[Business.Id, Business.Partial]] =
        IO.pure(Index(1, List((Business.Id(0), Business.empty))))
      override def read(queryParameters: ReadParameters)(id: Business.Id): IO[Business] = ???
    }

  val vote = new VoteAlg[IO] {
    override def getVotesForBusiness(context: Context)(business: Business.Id): IO[List[(Person.Id, Votum)]] = IO.pure(Nil)
    override def getVotesForPerson(context: Context)(person: Person.Id): IO[List[(Business.Id, Votum)]] = IO.pure(Nil)
  }

  val auth: AuthorizationAlg[IO] = (_, _, _) => IO.pure(true)
  val user: User = User("unprivileged", User.Email("mail"), "qux", Set.empty)


  val service  = new BusinessService(Component.Root, store, auth, vote)


  it should "serve requests" in {
    val request = AuthedRequest(user, Request[IO](method = Method.GET, uri = Uri.uri("index")))
    val result = service.service.run(request).value

    // TODO: find a way to use matchers better
    check(result, Ok, Some(List.empty[Business.Id].asJson)) shouldBe true
  }

  it should "be able to decode url parameters" in {
    val params = "?lp=50&os=0&ps=100&fields=Title,Description&orderBy=Title|Desc,Description|Desc&lang=de"

    val ps = params.drop(1).split('&').map { p =>
      val key :: value :: Nil = p.split('=').toList
      key -> List(value)
    }.toMap

    service.indexQueryParamDecoder.decode(Params(ps)) shouldBe
      Some(
        IndexQueryParameters(
          PageSize(100),
          Offset(0),
          Context(LegislativePeriod.Id(50), Language.German),
          List[(Business.Field, Sorting.Direction)](Business.Field.Title -> Descending, Business.Field.Description -> Descending),
          Set(Business.Field.Title, Business.Field.Description),
        )
      )
  }
}

object BusinessServiceSpec extends Matchers with Inside {
  def check[A](
    eventualResponse: IO[Option[Response[IO]]],
    expectedStatus: Status,
    expectedBody: Option[A])(
    implicit ev: EntityDecoder[IO, A]
  ): Boolean =  {
    val response = eventualResponse.unsafeRunSync()
    val statusCheck = response.forall(_.status == expectedStatus)
    val bodyCheck =
      expectedBody.
        fold(response.forall(_.body.compile.toVector.unsafeRunSync().isEmpty)){
          expected =>
            response.forall(_.as[A].unsafeRunSync() == expected)
        }

    statusCheck && bodyCheck
  }
}
