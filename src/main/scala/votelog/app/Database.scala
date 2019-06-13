package votelog.app

import cats.effect.{Async, ContextShift}
import doobie.util.transactor.Transactor

object Database {

  def buildTransactor[F[_]: Async: ContextShift](
    config: Configuration
  ): Transactor[F] =
    Transactor.fromDriverManager[F](
      driver = config.driver,
      url = config.url,
      user = config.user,
      pass = config.password,
    )

  case class Configuration(driver: String, url: String, user: String, password: String)
}
