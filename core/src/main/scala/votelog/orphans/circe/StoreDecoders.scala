package votelog.orphans.circe

import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.authentication.User
import votelog.domain.politics.{Motion, Ngo, Person}
import votelog.persistence.{NgoStore, UserStore}


trait MotionStoreDecoders {
  implicit val motionIdCirceDecoder: io.circe.Decoder[Motion.Id]
  implicit val personIdCirceDecoder: io.circe.Decoder[Person.Id]
}

trait PersonStoreDecoders {
  implicit val personIdCirceDecoder: io.circe.Decoder[Person.Id]
}

trait UserStoreDecoder {
  implicit val userIdCirceDecoder: io.circe.Decoder[User.Id]
  implicit val userEmailCirceDecoder: io.circe.Decoder[User.Email]
  implicit val userStorePasswordClearCirceDecoder: io.circe.Decoder[UserStore.Password.Clear] =
    deriveDecoder[UserStore.Password.Clear]
  implicit val userRecipeCirceDecoder: io.circe.Decoder[UserStore.Recipe] =
    deriveDecoder[UserStore.Recipe]
}

trait NgoStoreDecoder {
  implicit val ngoIdCirceDecoder: io.circe.Decoder[Ngo.Id]
  implicit val ngoRecipeCirceDecoder: io.circe.Decoder[NgoStore.Recipe] =
    deriveDecoder[NgoStore.Recipe]

}
