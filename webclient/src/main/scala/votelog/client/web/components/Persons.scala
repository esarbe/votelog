package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.Application.{personsComponent, personsStore}
import votelog.client.web.components.html.{DynamicList, StaticSelect}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, LegislativePeriod, Person}
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.{Group, Node, NodeBuffer}

class Persons(
  component: Component,
  persons: PersonStore[Future],
  language: Rx[votelog.domain.politics.Language],
) {

  val pagingConfiguration = Paging.Configuration(PageSize(10), Seq(PageSize(10), PageSize(20), PageSize(40)))
  val paging: Paging = new Paging(component.child("paging"), pagingConfiguration)
  var cache = collection.mutable.Map.empty[Person.Id, Rx[Person]]
  var viewCancelable: Option[Cancelable] = None

  val legislativePeriod =
    new StaticSelect(
      legend = "legislative period",
      options = LegislativePeriod.ids,
      default = LegislativePeriod.Default.id,
      clazz = "legislativePeriod",
      id = "legislativePeriod"
    )

  val queryParameters: Rx[Context] =
    for {
      language <- language
      legislativePeriod <- legislativePeriod.model
    } yield Context(legislativePeriod, language)


  // will run like: None -> Some(List) -> None -> Some(List)
  val unstableIds: Rx[Option[List[Person.Id]]] =
    for {
      offset <- paging.offset
      pageSize <- paging.pageSize
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
      offset <- paging.offset
      pageSize <- paging.pageSize
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
        language <- language
        person <- personsStore.read(language)(id).toRx
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
    <controls>
      <fieldset>
        { paging.view }
      </fieldset>
    </controls>
    <content
      mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node) }
      mhtml-onunmount={ (node: org.scalajs.dom.Node) => unountView(node) } />
  }

}
