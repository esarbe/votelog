package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import org.scalajs.dom.Element
import votelog.client.web.Application.personsService
import votelog.client.web.components.html.DynamicList
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.IndexQueryParameters
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, Person}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.xml.Node

class Persons(
  persons: ReadOnlyStoreAlg[Future, Person, Person.Id],
  queryParameters: Rx[Context],
  defaultPageSize: PageSize,
) extends Component[List[Person]] {

  val pageSize: Var[PageSize] = Var(defaultPageSize)
  val offset: Var[Offset] = Var(Offset(0))
  val validOffset = offset.keepIf(_.value >= 0)(Offset(0))
  var cache = collection.mutable.Map.empty[Person.Id, Rx[Person]]

  // wil run like: None -> Some(List) -> None -> Some(List)
  val unstableIds: Rx[Option[List[Person.Id]]] =
    for {
      offset <- validOffset
      pageSize <- pageSize
      queryParameters <- queryParameters
      indexQueryParams = IndexQueryParameters(pageSize, offset, queryParameters)
      ids <- personsService.index(indexQueryParams).toRx
    }  yield ids match {
      case Some(Success(ids)) => Some(ids)
      case Some(Failure(error)) => println(error.getMessage); None
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
      ids = personsService.index(indexQueryParams)
      persons <-
        ids
          .flatMap { ids => Future.sequence(ids.map(personsService.read(queryParameters.language))) }
          .toRx
          .collect { case Some(Success(persons)) => persons }(Nil)
    } yield persons
  }

  def setOffset: js.Dynamic => Unit = {
    event =>
      offset.update(_ => Offset(event.target.value.asInstanceOf[String].toLong))
  }

  def renderPerson(p: Person) = {
    <dl class="person" data-id={p.id.value.toString}>
      <dt class="name">Name</dt>
      <dd>{p.firstName.value} {p.lastName.value} </dd>
      <dt class="canton">Canton</dt>
      <dd>{p.canton.value}</dd>
      <dt class="party">Party</dt>
      <dd>{p.party}</dd>
    </dl>
  }

  def mountOn(parent: Element): Cancelable = {
    val render: Person.Id => Node = { (id: Person.Id) =>
      val person =
        for {
          qp <- queryParameters
          person <- personsService.read(qp.language)(id).toRx
        } yield person match {
          case Some(Success(person)) => Right(person)
          case Some(Failure(exception)) => Left(exception)
          case _ => Left(new RuntimeException("unknown failure"))
        }

      <div>
        { person.map {
            case Right(person) => renderPerson(person)
            case Left(exception) => <div> { exception.getMessage } </div>
          }
        }
      </div>
    }

    DynamicList.mountOn(parent, ids, render)
  }

  val view: Node =
    <section>
      <header>
        <fieldset>
            <dl>
              <dt><label for="offset">Offset</label></dt>
              <dd><input type="number" value={validOffset.map(_.value.toString)} onchange={setOffset}></input></dd>
            </dl>
        </fieldset>
      </header>
      <ul>
        {
          /*personRx.map {
            case Right(person) => <li>{person.id.toString}</li>
            case Left((error, id)) => <li><span>could not retrieve user {id.toString}: {error.getMessage}</span></li>
          }*/
        ""}
      </ul>
    </section>
}
