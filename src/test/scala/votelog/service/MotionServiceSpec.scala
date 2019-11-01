package votelog.service

import cats.effect.IO
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{AuthedRequest, EntityDecoder, Method, Request, Response, Status, Uri}
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.circe.implicits._
import votelog.domain.authentication.User
import votelog.domain.authorization.{AuthorizationAlg, Component}
import votelog.domain.politics.Motion
import votelog.infrastructure.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.persistence.MotionStore
import votelog.service.MotionServiceSpec.check


class MotionServiceSpec extends FlatSpec with Matchers {

  val store =
    new MotionStore[IO] {
      override def index(queryParameters: IndexQueryParameters): IO[List[Motion.Id]] = IO.pure(Nil)
      override def read(queryParameters: QueryParameters)(id: Motion.Id): IO[Motion] = ???
    }

  val auth: AuthorizationAlg[IO] = (_, _, _) => IO.pure(true)
  val user = User("unprivileged", User.Email("mail"), "qux", Set.empty)

  val service = new MotionService(Component.Root, store, auth).service

  it should "serve requests" in {
    val request = AuthedRequest(user, Request[IO](method = Method.GET, uri = Uri.uri("index")))
    val result = service.run(request).value

    // TODO: find a way to use matchers better
    check(result, Ok, Some(List.empty[Motion.Id].asJson)) shouldBe true
  }
}

object MotionServiceSpec extends Matchers with Inside {
  def check[A](
    eventualResponse: IO[Option[Response[IO]]],
    expectedStatus: Status,
    expectedBody: Option[A])(
    implicit ev: EntityDecoder[IO, A]
  ): Boolean =  {
    val response = eventualResponse.unsafeRunSync
    val statusCheck = response.forall(_.status == expectedStatus)
    val bodyCheck =
      expectedBody.
        fold(response.forall(_.body.compile.toVector.unsafeRunSync.isEmpty)){
          expected =>
            response.forall(_.as[A].unsafeRunSync == expected)
        }

    statusCheck && bodyCheck
  }
}
