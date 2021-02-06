package votelog.client.web.components

import cats.Show
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.implicits.showInterpolator
import mhtml.future.syntax._
import mhtml.{Rx, Var}
import votelog.client.web.components.html.tools.{ifEnter, trigger}
import votelog.domain.authorization.Component
import votelog.domain.crudi.StoreAlg

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.Elem

trait CrudCreateComponent[F, T, Identity, Recipe, Order] {
  val store: StoreAlg[Future, T, Identity, Recipe, Order]
  implicit val component: Component

  implicit def showIdentity: Show[Identity]

  type Error = (String, String)
  def validateRecipe(recipe: Recipe): Validated[NonEmptyList[Error], Recipe]
  val recipe: Var[Option[Recipe]] = Var(None)
  val submitCreate: Var[Unit] = Var(())

  private val validatedRecipe: Rx[Option[Validated[NonEmptyList[(String, String)], Recipe]]] =
    recipe.map(_.map(validateRecipe))

  private val errors: Rx[List[(String, String)]] =
    validatedRecipe
      .collect {
        case Some(Invalid(errors)) => errors.toList.map { case (field, message) => (id(field), message) }
      }(Nil)

  private val validRecipe: Rx[Option[Recipe]] =
    validatedRecipe.collect {
      case Some(Valid(recipe)) => Some(recipe)
      case _ => None
    }(None)

  private val result: Rx[Option[Either[Throwable, Identity]]] =
    validRecipe.sampleOn(submitCreate)
      .flatMap {
        case Some(recipe) => store.create(recipe).toRx
        case None => Rx(None)
      }
      .map {
        case None => None
        case Some(Success(id)) => Some(Right(id))
        case Some(Failure(error)) => Some(Left(error))
      }

  val model: Rx[Option[Identity]] =
    result.collect {
      case Some(Right(id)) => Option(id)
    }(None)

  private val error: Rx[Option[Throwable]] =
    result.collect {
      case Some(Left(error)) => Option(error)
    }(None)

  def renderForm(recipe: Var[Option[Recipe]], errors: Rx[List[Error]]): Elem

  def form(legend: String): Elem = {
    <article class="ngo">
      <fieldset onkeyup={ ifEnter(trigger(submitCreate)) }>
        <legend>{legend}</legend>

        { renderForm(recipe, errors) }

        <input type="button" value="create" onclick={ trigger(submitCreate) } disabled={ validRecipe.map(_.isDefined) } />
        {
        result.map {
          case Some(Right(id)) => <message type="info">Ngo {show"id"} created</message>
          case Some(Left(error)) => <message type="error">Error creating new Ngo: {error.getMessage }</message>
          case None => <message />
        }
        }
        <errors>

        </errors>
      </fieldset>
    </article>
  }

}
