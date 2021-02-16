package votelog.client.web.components

import cats.implicits._
import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.Application.personsStore
import votelog.client.web.components.html.{DynamicList, StaticSelect}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.PageSize
import votelog.domain.politics.{Business, Context, LegislativePeriod, Person, PersonPartial, Vote, VoteAlg, Votum}
import votelog.persistence.{BusinessStore, PersonStore}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.xml.{Group, Node}

class Persons(
  component: Component,
  persons: PersonStore[Future],
  businesses: BusinessStore[Future],
  voting: VoteAlg[Future],
  language: Rx[votelog.domain.politics.Language],
) {

  val pagingConfiguration: Paging.Configuration = Paging.Configuration(PageSize(10), Seq(PageSize(10), PageSize(20), PageSize(40)))
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

  val previewFields = Set(Person.Field.FirstName, Person.Field.LastName)

  val queryParameters: Rx[Context] =
    for {
      language <- language
      legislativePeriod <- legislativePeriod.model
    } yield Context(legislativePeriod, language)

  // will run like: None -> Some(List) -> None -> Some(List)
  val unstableIds: Rx[Option[Either[Throwable, List[(Person.Id, PersonPartial)]]]] =
    for {
      offset <- paging.offset
      pageSize <- paging.pageSize
      queryParameters <- queryParameters
      orderings = List(Person.Field.LastName, Person.Field.FirstName)
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters, orderings, previewFields)
      ids <- persons.index(indexQueryParams).map(_.entities).toRx
    }  yield ids match {
      case Some(Success(ids)) => Some(Right(ids))
      case Some(Failure(error)) => Some(Left(error))
      case None => None
    }

  // keeps last list, None doesn't appear
  val ids: Rx[List[(Person.Id, PersonPartial)]] = unstableIds.foldp(List.empty[(Person.Id, PersonPartial)]){
    case (_, Some(Right(ids))) => ids
    case (acc, _) => acc
  }

  val indexError: Rx[Iterable[Throwable]] = unstableIds.map(_.toList.flatMap(_.swap.toList)).dropRepeats

  val maybeSelected: Var[Option[Person.Id]] = Var(None)

  val selectedEntity: Rx[Option[Either[Throwable, Person]]] =
    for {
      selectedId <- maybeSelected
      language <- language
      entity = selectedId.map(id => persons.read(language)(id).toRx)
      maybeResult <- entity match {
        case Some(rx) => rx
        case None => Rx(None)
      }
    } yield maybeResult.map(_.toEither)

  val maybeEntity: Rx[Option[Person]] =
    selectedEntity.collect {
      case Some(Right(person)) => Option(person)
    }(None)

  val maybeIdOrEntity: Rx[Option[Either[Person.Id, Person]]] =
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
      orderings = List(Person.Field.LastName, Person.Field.FirstName)
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters, orderings, previewFields)
      ids = persons.index(indexQueryParams)
      persons <-
        ids
          .flatMap { index => Future.sequence(index.entities.map { case (id, _) => personsStore.read(queryParameters.language)(id)}) }
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

  val votes =
    for {
      maybeSelected <- maybeSelected
      context <- queryParameters
      language <- language
      votes <- maybeSelected match {
        case Some(id) =>
          voting
            .getVotesForPerson(context)(id)
            .flatMap { votes =>
              Future.sequence(votes.map { case (businessId, votum) =>
                businesses.read(language)(businessId).map(business => (business, votum))
              })
            }.toRx.collect {
            case Some(Success(votes)) => Right(votes)
            case Some(Failure(error)) => Left(error)
          }(Right(Nil))
        case None => Rx(Right(Nil))
      }
    } yield votes

  def renderVotes: Either[Throwable, List[(Business, Votum)]] => Node = {
    case Right(votes) =>
        Group(votes.map {
          case (business, votum) =>
            <dl class="entity list vote">
              <dt> {business.title.getOrElse("Unknown business")} </dt>
              <dd> {Votum.asString(votum)} </dd>
            </dl>
          })
    case Left(error) =>
      <p>
        Error occured: {error.getMessage}
      </p>
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

  def renderPartial (ref: Person.Id, partial: PersonPartial): Rx[Node] = {
    Rx({
      <a href={s"#${id(ref.value.toString)(component)}"}>
        <span class="entity person" data-id={ref.value.toString}>
          {
            for {
              firstName <- partial.firstName
              lastName <- partial.lastName
            } yield firstName.value + " " + lastName.value
          }
        </span>
      </a>
    })
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

    viewCancelable = Some(DynamicList.mountOn(ids, (renderPartial _).tupled)(e))
  }

  def umountView(e: org.scalajs.dom.Node): Unit = {
    viewCancelable.foreach(_.cancel)
  }

  val view = Group {
    <controls>
      { paging.view }
    </controls>

    <article class="person">
      <ul class="index"
        mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node) }
        mhtml-onunmount={ (node: org.scalajs.dom.Node) => umountView(node) }
      />

      { maybeIdOrEntity.map(renderEntity) }
      { votes.map(renderVotes) }

    </article>

    <messages>
      { errors.map { _.toList.map { error => <error> { error.getMessage } </error> } } }
    </messages>
  }

}
