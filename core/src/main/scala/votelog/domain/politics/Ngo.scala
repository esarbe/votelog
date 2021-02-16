package votelog.domain.politics

case class Ngo(name: String)

object Ngo {

  case class Partial(name: Option[String])

  val empty = Partial(None)
  case class Id(value: String)

  sealed trait Fields
  object Fields {
    case object Name extends Fields

    val values: Set[Fields] = Set(Name)

    lazy val fromString: (String => Fields) =
      (values zip values).map({ case (key, value) => (key.toString, value) }).toMap.apply
  }
}
