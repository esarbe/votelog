package votelog

import cats.effect.IO
import endpoints4s.algebra.BasicAuthentication
import endpoints4s.{algebra, http4s, openapi}
import endpoints4s.openapi.model.{Info, OpenApi}
import org.http4s.{BasicCredentials, Header, Headers, HttpRoutes, Method, Request}
import org.http4s.util.CaseInsensitiveString
import votelog.app.TestAuth.AuthedRoute.{authenticated, routesFromEndpoints}
import votelog.app.TestAuth.DocumentedRoute.{authenticated, openApi}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import votelog.endpoint.Authentication
import votelog.endpoints.algebra.SessionEndpoint

/**
 * test the integration of AuthenticationEndpoints and http4s
 */
class AuthenticationEndpointsSpec extends AnyFlatSpec with Matchers {

  object SessionRoute
    extends http4s.server.Endpoints[IO]
    with SessionEndpoint
    with http4s.server.BasicAuthentication
    with http4s.server.JsonEntitiesFromSchemas {

    val authImpl = authenticated.implementedByEffect {
      case BasicAuthentication.Credentials("foo", "bar") =>
        IO(Some(("success?", "Authentication: Bearer: foobar")))
      case _ =>
        IO(None)
    }
  }
  case class AuthenticationToken(user: String, token: String)

  trait AuthenticationEndpoint
    extends Authentication {



    override def authenticationToken: Response[AuthenticationToken]
  }


  val service = HttpRoutes.of(routesFromEndpoints(SessionRoute.authImpl))

  it should "forbid a login with invalid user" in {
    val request = Request[IO](
      method = Method.POST,
      uri = uri"/api/v0/session",
      headers = Headers.of(Header("Authorization", s"Basic ${BasicCredentials("foo", "invalid").token}"))
    )

    val result = service.orNotFound.run(request).unsafeRunSync()
    result.status.code shouldBe 403
    result.headers.get(CaseInsensitiveString("Set-Cookie")) shouldBe None
  }

  it should "allow a login with valid user" in {
    val request = Request[IO](
      method = Method.POST,
      uri = uri"/api/v0/session",
      headers = Headers.of(Header("Authorization", s"Basic ${BasicCredentials("foo", "bar").token}"))
    )

    val result = service.orNotFound.run(request).unsafeRunSync()
    result.status.code shouldBe 200
    result.headers.get(CaseInsensitiveString("Set-Cookie")).map(_.value) shouldBe Some("Authentication: Bearer: foobar")
  }

  it should "set a cookie header with a valid authorization token" in {

  }

  it should "allow access on protected routes with a valid authorization token" in {



  }

}
