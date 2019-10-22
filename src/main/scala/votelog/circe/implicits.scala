package votelog.circe

object implicits
  extends MotionStoreDecoders
  with PersonStoreDecoders
  with UserStoreDecoder
  with NgoStoreDecoder
  with ModelDecoders
  with ModelEncoders
