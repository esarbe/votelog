package votelog.orphans.doobie.domain.politics

import doobie.util.Read
import doobie.util.meta.Meta
import votelog.domain.politics.Person.Gender
import votelog.domain.politics.Person.Gender.{Female, Male}

trait PersonInstances {

  implicit val genderRead: Meta[Gender] =
    Meta[String].imap {
      case "f" | "F" => Female
      case "m" | "M" => Male
    }{
      case Female => "f"
      case Male => "m"
    }
}


