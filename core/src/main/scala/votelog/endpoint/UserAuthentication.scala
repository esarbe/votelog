package votelog.endpoint

trait UserAuthentication extends Authentication {
  override type AuthenticationToken = UserAuthentication.UserToken
}


object UserAuthentication {
  case class UserToken(username: String, token: String)
}
