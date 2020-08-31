package votelog.endpoint

import endpoints4s.{Tupler, algebra}

/**
 * Algebra interface for defining authenticated endpoints.
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

  /**
   * A request with the given `method`, `url` and `entity`, and which is rejected by the server if it
   * doesn’t contain a valid JWT.
   */
  private[authentication] def authenticatedRequest[U, E, UE, UET](
    method: Method,
    url: Url[U],
    entity: RequestEntity[E]
  )(implicit
    tuplerUE: Tupler.Aux[U, E, UE],
    tuplerUET: Tupler.Aux[UE, AuthenticationToken, UET]
  ): Request[UET]

  /** A response that might signal to the client that his request was not authenticated.
   * Clients throw an exception if the response status is `Unauthorized`.
   * Servers build an `Unauthorized` response in case the incoming request was not correctly authenticated.
   */
  private[authentication] def wheneverAuthenticated[A](
    response: Response[A]
  ): Response[A]

  /**
   * User-facing constructor for endpoints requiring authentication.
   *
   * @return An endpoint requiring a authentication information to be provided
   *         in the `Authorization` request header. It returns `response`
   *         if the request is correctly authenticated, otherwise it returns
   *         an empty `Unauthorized` response.
   *
   * @param method        HTTP method
   * @param url           Request URL
   * @param response      HTTP response
   * @param requestEntity HTTP request entity
   * @tparam U Information carried by the URL
   * @tparam E Information carried by the request entity
   * @tparam R Information carried by the response
   */
  final def authenticatedEndpoint[U, E, R, UE, UET](
    method: Method,
    url: Url[U],
    requestEntity: RequestEntity[E],
    response: Response[R]
  )(implicit
    tuplerUE: Tupler.Aux[U, E, UE],
    tuplerUET: Tupler.Aux[UE, AuthenticationToken, UET]
  ): Endpoint[UET, R] =
    endpoint(
      authenticatedRequest(method, url, requestEntity),
      wheneverAuthenticated(response)
    )

}