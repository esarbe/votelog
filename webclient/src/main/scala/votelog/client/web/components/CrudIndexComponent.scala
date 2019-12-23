package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.{Rx, Var}
import votelog.client.mhtml.mount.Embeddable
import votelog.client.web.components.html.DynamicList
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.crudi.StoreAlg

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.{Group, Node}

abstract class CrudIndexComponent[T, Identity, Recipe](
  val store: StoreAlg[Future, T, Identity, Recipe],
  defaultPageSize: PageSize,
) {
  def indexQueryParameters: Rx[store.IndexQueryParameters]
  def queryParameters: Rx[store.QueryParameters]
  def queryParametersView: Option[Node]

  val pageSize: Var[PageSize] = Var(defaultPageSize)
  val offset: Var[Offset] = Var(Offset(0))
  val validOffset = offset.keepIf(_.value >= 0)(Offset(0))

  // will run like: None -> Some(List) -> None -> Some(List)
  val unstableIds: Rx[Option[List[Identity]]] =
    for {
      indexQueryParameters <- indexQueryParameters
      ids <- store.index(indexQueryParameters).toRx
    } yield ids.flatMap {
      case Success(ids) => Some(ids)
      case Failure(error) => println(s"error: ${error.getMessage}"); None
    }

  // keeps last list, None doesn't appear
  val ids: Rx[List[Identity]] = unstableIds.foldp(List.empty[Identity]){
    case (acc, curr) => println(s"collecting $curr"); curr.getOrElse(acc)
  }

  val model: Rx[Option[T]] = {
    for {
      queryParameters <- queryParameters
      _ = println("fooo")
      id <- selected
      entities <-
        Future.sequence(id.toList.map(store.read(queryParameters)))
          .toRx
          .collect { case Some(Success(entities)) => entities.headOption }(None)
    } yield entities
  }

  def setOffset: js.Dynamic => Unit = {
    event =>
      offset := Offset(event.target.value.asInstanceOf[String].toLong)
  }

  val selected: Var[Option[Identity]] = Var(None)

  def render(renderEntity: (Identity, T) => Rx[Node]): Identity => Rx[Node] = { (id: Identity) =>
    val person: Rx[Either[Throwable, Option[T]]] =
      for {
        qp <- queryParameters
        _ = println("bar")
        entity <- store.read(qp)(id).toRx
      } yield entity match {
        case Some(Success(person)) => Right(Some(person))
        case Some(Failure(exception)) => Left(exception)
        case None => Right(None)
      }

    person.map {
      case Right(Some(entity)) =>
        <div onclick={ (_: Any) => selected := Some(id) } > { renderEntity(id, entity) } </div>
      case Right(None) =>
        <div onclick={ (_: Any) => selected := Some(id) } class="loading"></div>
      case Left(exception) => <div> { exception.getMessage }</div>
    }
  }

  def view(renderEntity: (Identity, T) => Rx[Node]) =  Group {
    <header>
      <fieldset>
        <dl>
          <dt><label for="offset">Offset</label></dt>
          <dd><input type="number" value={validOffset.map(_.value.toString)} onchange={setOffset}></input></dd>
        </dl>
      </fieldset>
      { queryParametersView.map(view => <fieldset> {view} </fieldset>) }
    </header>
    <content>
      { Embeddable(<ul/>, DynamicList.mountOn(ids, render(renderEntity))) }
    </content>
  }

}