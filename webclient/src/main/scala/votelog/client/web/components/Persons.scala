package votelog.client.web.components

import mhtml.Rx
import cats._
import cats.implicits._
import cats.instances.future._
import mhtml.{Rx, Var}
import mhtml.future.syntax._
import votelog.client.web.Application.personsService
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.politics.{Context, Person}

import scala.concurrent.Future
import scala.util.Success
import scala.xml.Node
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

class Persons(
  persons: ReadOnlyStoreAlg[Future, Person, Person.Id],
  queryParameters: Rx[Context],
  defaultPageSize: PageSize,
) extends Component[List[Person]] {

  val pageSize: Var[PageSize] = Var(defaultPageSize)
  val offset: Var[Offset] = Var(Offset(0))
  val validOffset = offset.keepIf(_.value >= 0)(Offset(0))

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
    } yield  persons
  }

  def setOffset: js.Dynamic => Unit = {
    event =>
      offset.update(_ => Offset(event.target.value.asInstanceOf[String].toLong))
  }

  def renderPerson(p: Person) = {
    <dl>
      <dt>Name</dt>
      <dd> {p.firstName.value} {p.lastName.value} </dd>
      <dt>Canton</dt>
      <dd>{p.canton.value}</dd>
    </dl>
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
        { model.map { persons => persons.map(renderPerson)} }
      </ul>
    </section>


}
