package votelog.circe

object implicits
  extends MotionStoreDecoders
  with PoliticianStoreDecoders
  with UserStoreDecoder
  with NgoStoreDecoder
  with ModelDecoders
  with ModelEncoders
