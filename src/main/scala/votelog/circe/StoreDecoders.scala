package votelog.circe

import io.circe
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.authorization.User
import votelog.domain.politics.{Motion, Ngo, Person}
import votelog.persistence.{NgoStore, UserStore}


trait MotionStoreDecoders {
  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id]
  implicit val personIdCirceDecoder: circe.Decoder[Person.Id]
}

trait PersonStoreDecoders {
  implicit val personIdCirceDecoder: circe.Decoder[Person.Id]
}

trait UserStoreDecoder {
  implicit val userIdCirceDecoder: circe.Decoder[User.Id]
  implicit val userEmailCirceDecoder: circe.Decoder[User.Email]
  implicit val userStorePasswordClearCirceDecoder: circe.Decoder[UserStore.Password.Clear] =
    deriveDecoder[UserStore.Password.Clear]
  implicit val userRecipeCirceDecoder: circe.Decoder[UserStore.Recipe] =
    deriveDecoder[UserStore.Recipe]
}

trait NgoStoreDecoder {
  implicit val ngoIdCirceDecoder: circe.Decoder[Ngo.Id]
  implicit val ngoRecipeCirceDecoder: circe.Decoder[NgoStore.Recipe] =
    deriveDecoder[NgoStore.Recipe]

}
