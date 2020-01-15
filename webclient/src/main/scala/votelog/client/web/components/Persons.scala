package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.Application.{personsComponent, personsStore}
import votelog.client.web.components.html.DynamicList
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, Person}
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.{Group, Node, NodeBuffer}

class Persons(
  persons: PersonStore[Future],
  queryParameters: Rx[Context],
  defaultPageSize: PageSize,
) {

  val pageSize: Var[PageSize] = Var(defaultPageSize)
  val offset: Var[Offset] = Var(Offset(0))
  val validOffset: Rx[Offset] = offset.keepIf(_.value >= 0)(Offset(0))
  var cache = collection.mutable.Map.empty[Person.Id, Rx[Person]]
  var viewCancelable: Option[Cancelable] = None

  // will run like: None -> Some(List) -> None -> Some(List)
  val unstableIds: Rx[Option[List[Person.Id]]] =
    for {
      offset <- validOffset
      pageSize <- pageSize
      queryParameters <- queryParameters
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters)
      ids <- personsStore.index(indexQueryParams).toRx
    }  yield ids match {
      case Some(Success(ids)) => Some(ids)
      case Some(Failure(error)) => println(s"error: $error, ${error.getMessage}"); None
      case None => None
    }

  // keeps last list, None doesn't appear
  val ids: Rx[List[Person.Id]] = unstableIds.foldp(List.empty[Person.Id]){
    case (acc, curr) => curr.getOrElse(acc)
  }

  val model: Rx[List[Person]] = {
    for {
      offset <- validOffset
      pageSize <- pageSize
      queryParameters <- queryParameters
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters)
      ids = personsStore.index(indexQueryParams)
      persons <-
        ids
          .flatMap { ids => Future.sequence(ids.map(personsStore.read(queryParameters.language))) }
          .toRx
          .collect { case Some(Success(persons)) => persons }(Nil)
    } yield persons
  }

  def setOffset: js.Dynamic => Unit = {
    event =>
      offset.update(_ => Offset(event.target.value.asInstanceOf[String].toLong))
  }

  def renderPerson: Either[Person.Id, Person] => Node = {
    case Right(p) =>
      /* <dl class="person" data-id={p.id.value.toString}>
        <dt class="name">Name</dt>
        <dd>{p.firstName.value} {p.lastName.value} </dd>
        <dt class="canton">Canton</dt>
        <dd>{p.canton.value}</dd>
        <dt class="party">Party</dt>
        <dd>{p.party}</dd>
      </dl> */
      <dl class="person" data-id={p.id.value.toString}>
        <dl><dd>Name</dd><dt>{p.firstName.value} {p.lastName.value}</dt></dl>
      </dl>
    case Left(id) =>
      <dl class="person loading" data-id={id.value.toString}></dl>
  }

  val render: Person.Id => Rx[Node] = { (id: Person.Id) =>
    val person: Rx[Either[Throwable, Either[Person.Id, Person]]] =
      for {
        qp <- queryParameters
        person <- personsStore.read(qp.language)(id).toRx
      } yield person match {
        case Some(Success(person)) => Right(Right(person))
        case Some(Failure(exception)) => Left(exception)
        case None => Right(Left(id))
      }

      person.map {
        case Right(person) => renderPerson(person)
        case Left(exception) => <div> { exception.getMessage } </div>
      }
  }

  def mountView(e: org.scalajs.dom.Node) = {
    viewCancelable = Some(DynamicList.mountOn(ids, render)(e))
  }

  def unountView(e: org.scalajs.dom.Node) = {
    viewCancelable.foreach(_.cancel)
  }

  val view = Group {
      <header>
        <fieldset>
          <dl>
            <dt><label for="offset">Offset</label></dt>
            <dd><input type="number" value={validOffset.map(_.value.toString)} onchange={setOffset}></input></dd>
          </dl>
        </fieldset>
      </header>
      <content
        mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node) }
        mhtml-onunmount={ (node: org.scalajs.dom.Node) => unountView(node) } />
  }

}
