package votelog.service

import votelog.circe.implicits._
import votelog.domain.authorization.User
import votelog.encoders.implicits._
import votelog.infrastructure.StoreService
import votelog.persistence.UserStore

abstract class UserService extends StoreService[User, User.Id, UserStore.Recipe]
