package votelog.circe

import io.circe
import io.circe.generic.semiauto.deriveDecoder
import votelog.domain.model.{Motion, Politician}
import votelog.persistence.{MotionStore, PoliticianStore}


trait MotionStoreDecoders {
  implicit val motionIdCirceDecoder: circe.Decoder[Motion.Id]
  implicit val politicianIdCirceDecoder: circe.Decoder[Politician.Id]
  implicit val motionRecipeCirceDecoder: circe.Decoder[MotionStore.Recipe] = deriveDecoder[MotionStore.Recipe]
}

trait PoliticianStoreDecoders {
  implicit val politicianRecipeCirceDecoder: circe.Decoder[PoliticianStore.Recipe] = deriveDecoder[PoliticianStore.Recipe]

}