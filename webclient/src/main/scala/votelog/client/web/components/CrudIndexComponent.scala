package votelog.client.web.components

import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.components.html.DynamicList
import votelog.domain.crudi.ReadOnlyStoreAlg

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Node}

trait CrudIndexComponent[T, Identity] { self =>

  val store: ReadOnlyStoreAlg[Future, T, Identity]
  val indexQueryParameters: Rx[store.IndexQueryParameters]
  val queryParameters: Rx[store.QueryParameters]

  var viewCancelable: Option[Cancelable] = None
  val selectedId: Var[Option[Identity]] = Var(None)

  // will run like: None -> Some(List) -> None -> Some(List)
  // TODO: add better error handling
  val unstableIds: Rx[Option[List[Identity]]] =
    for {
      indexQueryParameters <- indexQueryParameters
      ids <- store.index(indexQueryParameters).toRx
    } yield ids.flatMap {
      case Success(ids) => Some(ids)
      case Failure(error) => println(s"error: $error, ${error.getMessage}"); None
    }

  // keeps last list, None doesn't appear
  val ids: Rx[List[Identity]] = unstableIds.foldp(List.empty[Identity]){
    case (acc, curr) => curr.getOrElse(acc)
  }

  private def mountView(e: org.scalajs.dom.Node, renderEntity: (Identity, T) => Rx[Node]): Unit = {
    val cancel = DynamicList.mountOn(ids, renderEntities(renderEntity))(e)
    viewCancelable = Some(cancel)
  }

  private def unmountView(e: org.scalajs.dom.Node): Unit = {
    viewCancelable.foreach(_.cancel)
  }

  // find a better way to group views/models
  val selectedEntity: Rx[Option[T]] = {
    for {
      queryParameters <- queryParameters
      id <- selectedId
      entity <- id match {
        case Some(id) => store.read(queryParameters)(id).toRx
        case None => Rx(None)
      }
    } yield entity match {
      case Some(Success(entity)) => Some(entity)
      case _ => None
    }
  }

  def renderIndex(renderEntity: (Identity, T) => Rx[Node]): Elem =  {
    <content>
      <ul mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node, renderEntity) }
          mhtml-onunmount={ (node: org.scalajs.dom.Node) => unmountView(node) } />
    </content>
  }

  private def renderEntities(renderEntity: (Identity, T) => Rx[Node]): Identity => Rx[Node] = { (id: Identity) =>
    val entity: Rx[Either[Throwable, Option[T]]] =
      for {
        qp <- queryParameters
        entity <- store.read(qp)(id).toRx
      } yield entity match {
        case Some(Success(person)) => Right(Some(person))
        case Some(Failure(exception)) => Left(exception)
        case None => Right(None)
      }

    entity.map {
      case Right(Some(entity)) =>
        <div onclick={ (_: Any) => selectedId := Some(id) } > { renderEntity(id, entity) } </div>
      case Right(None) =>
        <div onclick={ (_: Any) => selectedId := Some(id) } class="loading"></div>
      case Left(exception) => <div> { exception.getMessage }</div>
    }
  }
}
