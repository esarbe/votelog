package votelog.domain.politics

case class Ngo(name: String)

object Ngo {
  case class Id(value: String)

  sealed trait Ordering
  object Ordering {
    case object Name extends Ordering

    val values: Set[Ordering] = Set(Name)

    lazy val fromString: (String => Ordering) =
      (values zip values).map({ case (key, value) => (key.toString, value) }).toMap.apply
  }
}
