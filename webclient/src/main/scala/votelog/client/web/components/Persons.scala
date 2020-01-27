package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.implicits.cats._
import cats.implicits._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.Application.{personsComponent, personsStore}
import votelog.client.web.components.html.{DynamicList, StaticSelect}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, LegislativePeriod, Person, VoteAlg}
import votelog.persistence.PersonStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
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
  val unstableIds: Rx[Option[Either[Throwable, List[Person.Id]]]] =
    for {
      offset <- paging.offset
      pageSize <- paging.pageSize
      queryParameters <- queryParameters
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters)
      ids <- personsStore.index(indexQueryParams).toRx
    }  yield ids match {
      case Some(Success(ids)) => Some(Right(ids))
      case Some(Failure(error)) => Some(Left(error))
      case None => None
    }

  // keeps last list, None doesn't appear
  val ids: Rx[List[Person.Id]] = unstableIds.foldp(List.empty[Person.Id]){
    case (_, Some(Right(ids))) => ids
    case (acc, _) => acc
  }

  val indexError: Rx[Iterable[Throwable]] = unstableIds.map(_.toList.flatMap(_.swap.toList)).dropRepeats

  val maybeSelected: Var[Option[Person.Id]] = Var(None)

  val selectedEntity: Rx[Option[Either[Throwable, Person]]] =
    for {
      selectedId <- maybeSelected
      language <- language
      entity = selectedId.map(id => personsStore.read(language)(id).toRx)
      maybeResult <- entity match {
        case Some(rx) => rx
        case None => Rx(None)
      }
    } yield maybeResult.map(_.toEither)

  val maybeEntity: Rx[Option[Person]] =
    selectedEntity.collect {
      case Some(Right(person)) => Option(person)
    }(None)

  val maybeIdOrEntity =
    maybeSelected.map(_.map(Left.apply))
        .merge(maybeEntity.map(_.map(Right.apply)))

  val entityError: Rx[Iterable[Throwable]] =
    selectedEntity.collect {
      case Some(Left(failure)) => List(failure)
    }(Nil)

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

  val errors: Rx[Iterable[Throwable]] = indexError.merge(entityError)

  def previewPerson: Either[Person.Id, Person] => Node = {
    case Right(p) =>
      <a href={s"#${id(p.id.value.toString)(component)}"}>
        <dl class="entity person" data-id={p.id.value.toString}>
          <dt>Name</dt><dd>{p.firstName.value} {p.lastName.value}</dd>
        </dl>
      </a>

    case Left(id) =>
      <dl class="entity person loading" data-id={id.value.toString}></dl>
  }

  def renderEntity: Option[Either[Person.Id , Person]] => Node = {
    case Some(Right(entity)) =>
      <dl class="entity person" data-id={entity.id.value.toString}>
        <dt class="name">Name</dt>
        <dd>{entity.firstName.value} {entity.lastName.value} </dd>
        <dt class="canton">Canton</dt>
        <dd>{entity.canton.value}</dd>
        <dt class="party">Party</dt>
        <dd>{entity.party}</dd>
      </dl>
    case Some(Left(id)) => <dl class="loading entity person" data-id={id.value.toString}></dl>
    case None => <dl class="empty entity person"></dl>
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
        case Right(person) => previewPerson(person)
        case Left(exception) => <div> { exception.getMessage } </div>
      }
  }

  def mountView(e: org.scalajs.dom.Node): Unit = {
    viewCancelable = Some(DynamicList.mountOn(ids, render)(e))
  }

  def unountView(e: org.scalajs.dom.Node): Unit = {
    viewCancelable.foreach(_.cancel)
  }

  val view = Group {
    <controls>
      { paging.view }
    </controls>

    <article class="person">
      <ul class="index"
        mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node) }
        mhtml-onunmount={ (node: org.scalajs.dom.Node) => unountView(node) }
      />

      { maybeIdOrEntity.map(renderEntity) }

    </article>

    <messages>
      { errors.map { _.toList.map { error => <error> { error.getMessage } </error> } } }
    </messages>
  }

}
