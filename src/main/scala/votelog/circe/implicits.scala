package votelog.circe

object implicits
  extends MotionStoreDecoders
  with PoliticianStoreDecoders
  with UserStoreDecoder
  with ModelDecoders
  with ModelEncoders
