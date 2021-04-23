package votelog.domain.data

object Sorting {
  sealed trait Direction extends Product with Serializable
  object Direction {
    case object Ascending extends Direction
    case object Descending extends Direction
  }

}
