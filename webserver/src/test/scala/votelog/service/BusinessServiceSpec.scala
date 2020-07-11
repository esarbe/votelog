package votelog.service

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
import votelog.domain.politics.{Business, Context, Language, LegislativePeriod, Person, VoteAlg, Votum}
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters, QueryParameters}
import votelog.persistence.BusinessStore
import votelog.service.BusinessServiceSpec.check

class BusinessServiceSpec extends AnyFlatSpec with Matchers {

  val store =
    new BusinessStore[IO] {
      override def index(queryParameters: IndexQueryParameters): IO[Index[Business.Id]] = IO.pure(Index(1, List(Business.Id(0))))
      override def read(queryParameters: QueryParameters)(id: Business.Id): IO[Business] = ???
    }

  val vote = new VoteAlg[IO] {
    override def getVotesForBusiness(context: Context)(business: Business.Id): IO[List[(Person.Id, Votum)]] = IO.pure(Nil)
    override def getVotesForPerson(context: Context)(person: Person.Id): IO[List[(Business.Id, Votum)]] = IO.pure(Nil)
  }

  val auth: AuthorizationAlg[IO] = (_, _, _) => IO.pure(true)
  val user = User("unprivileged", User.Email("mail"), "qux", Set.empty)

  val service = new BusinessService(Component.Root, store, auth, vote).service

  it should "serve requests" in {
    val request = AuthedRequest(user, Request[IO](method = Method.GET, uri = Uri.uri("index")))
    val result = service.run(request).value

    // TODO: find a way to use matchers better
    check(result, Ok, Some(List.empty[Business.Id].asJson)) shouldBe true
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
