package votelog.endpoints.algebra

import endpoints4s.algebra.BasicAuthentication

trait SessionEndpoint extends BasicAuthentication  {
  val authenticated: Endpoint[BasicAuthentication.Credentials, Option[(String, String)]] =
    authenticatedEndpoint(
      Post,
      path / "api" / "v0" / "session",
      ok(
        textResponse,
        Some("Create a session, token returned in 'Authorization' cookie"),
        responseHeader("Set-Cookie"))
    )
}

