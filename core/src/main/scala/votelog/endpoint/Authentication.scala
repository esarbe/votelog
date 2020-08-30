package votelog.endpoint

import endpoints4s.algebra

/**
 * Algebra interface for defining authenticated endpoints using JWT.
 */
trait Authentication extends algebra.Endpoints {

  /** Authentication information */
  type AuthenticationToken

  /** A response entity containing the authenticated user info
   *
   * Clients decode the JWT attached to the response.
   * Servers encode the authentication information as a JWT and attach it to their response.
   */
  def authenticationToken: Response[AuthenticationToken]

  /** A response that might signal to the client that his request was invalid using
   * a `BadRequest` status.
   * Clients map `BadRequest` statuses to `None`, and the underlying `response` into `Some`.
   * Conversely, servers build a `BadRequest` response on `None`, or the underlying `response` otherwise.
   */
  final def wheneverValid[A](responseA: Response[A]): Response[Option[A]] =
    responseA
      .orElse(response(BadRequest, emptyResponse))
      .xmap(_.fold[Option[A]](Some(_), _ => None))(_.toLeft(()))

}