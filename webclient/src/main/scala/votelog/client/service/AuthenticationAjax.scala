package votelog.client.service

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import votelog.client.Context
import votelog.domain.authentication.Authentication.Credentials.UserPassword
import votelog.domain.authentication.{Authentication, AuthenticationAlg, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationAjax(context: Context) extends AuthenticationAlg[Future] {

  override def login(cred: Authentication.Credentials): Future[Either[String, User]] = {
    cred match {
      case UserPassword(username, password) =>
        val basicAuthCreds = s"Basic  ${dom.window.btoa(s"$username:$password")}"
        Ajax.post(
          url = context.url + "/auth/login",
          headers = Map("Authorization" -> basicAuthCreds)
        ).map { res =>
          if (200 <= res.status && res.status < 300)
            Right(User("Carl", User.Email("foo@bar.qux"), "", Set.empty))
          else
            Left(res.responseText)
        }
    }
  }

}
