package votelog.persistence


import java.util.UUID
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.StoreAlg
import votelog.domain.data.Sorting.Direction
import votelog.domain.politics.{Ngo, Scoring}
import votelog.persistence.NgoStore.Recipe

//TODO: Scoring should be it's own store
trait NgoStore[F[_]]
  extends StoreAlg[F, Ngo, Ngo.Id, Recipe, Ngo.Partial, Unit, IndexQueryParameters[Unit, Ngo.Field, Ngo.Field]]
    with Scoring[F]

object NgoStore {
  case class Recipe(name: String)

  def validateRecipe(name: String): Validated[NonEmptyList[(String, String)], Recipe] =
    if (name.nonEmpty) Valid(Recipe(name))
    else Invalid("name" -> "must not be empty").toValidatedNel

  def newId: Ngo.Id = Ngo.Id(UUID.randomUUID.toString)
}