package votelog.circe

import io.circe
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.authorization.User
import votelog.domain.politics.{Motion, Ngo, Politician}
import votelog.persistence.{MotionStore, NgoStore, PoliticianStore, UserStore}


trait MotionStoreDecoders {
  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id]
  implicit val politicianIdCirceDecoder: circe.Decoder[Politician.Id]
  implicit val motionRecipeCirceDecoder: circe.Decoder[MotionStore.Recipe] =
    deriveDecoder[MotionStore.Recipe]
}

trait PoliticianStoreDecoders {
  implicit val politicianIdCirceDecoder: circe.Decoder[Politician.Id]
  implicit val politicianRecipeCirceDecoder: circe.Decoder[PoliticianStore.Recipe] =
    deriveDecoder[PoliticianStore.Recipe]
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
