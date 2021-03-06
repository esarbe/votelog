package votelog.domain.politics

case class Ngo(name: String)

object Ngo {

  case class Partial(name: Option[String])

  val empty = Partial(None)
  case class Id(value: String)

  sealed trait Field
  object Field {
    case object Name extends Field

    val values: Set[Field] = Set(Name)

    lazy val fromString: (String => Field) =
      (values zip values).map({ case (key, value) => (key.toString, value) }).toMap.apply
  }
}
