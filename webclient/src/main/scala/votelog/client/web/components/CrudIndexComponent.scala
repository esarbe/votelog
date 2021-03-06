package votelog.client.web.components

import cats.Id
import mhtml.future.syntax._
import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.components.html.DynamicList
import votelog.domain.crudi.ReadOnlyStoreAlg
import votelog.domain.crudi.ReadOnlyStoreAlg.Index

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.{Elem, Node}

trait CrudIndexComponent[T, Identity, Partial, ReadParameters, IndexParameters] { self =>

  val store: ReadOnlyStoreAlg[Future, T, Identity, Partial, ReadParameters, IndexParameters]
  val indexQueryParameters: Rx[IndexParameters]
  val queryParameters: Rx[ReadParameters]

  var viewCancelable: Option[Cancelable] = None
  val selectedId: Var[Option[Identity]] = Var(None)

  // will run like: None -> Some(List) -> None -> Some(List)
  // TODO: add better error handling
  lazy val unstableIndex: Rx[Option[Index[Identity, Partial]]] =
    for {
      indexQueryParameters <- indexQueryParameters
      ids <- store.index(indexQueryParameters).toRx
    } yield ids.flatMap {
      case Success(ids) => Some(ids)
      case Failure(error) => println(s"error: $error, ${error.getMessage}"); None
    }

  // keeps last list, None doesn't appear
  lazy val ids: Rx[List[(Identity, Partial)]] = unstableIndex.map(_.map(_.entities)).foldp(List.empty[(Identity, Partial)]){
    case (acc, curr) => curr.getOrElse(acc)
  }

  lazy val entitiesCount = unstableIndex.map(_.map(_.totalEntities)).foldp(0) {
    case (acc, curr) => curr.getOrElse(acc)
  }

  private def mountView(e: org.scalajs.dom.Node, renderEntity: (Identity, T) => Node): Unit = {
    println(s"ids: $ids, renderEntity: $renderEntity, renderEntities: ${ renderEntities _} ")
    val cancel = DynamicList.mountOn(ids.map(_.map(_._1)), renderEntities(renderEntity))(e)
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

  def renderIndex(renderEntity: (Identity, T) => Node): Elem =  {
    <ul class="index"
      mhtml-onmount={ (node: org.scalajs.dom.Node) => mountView(node, renderEntity) }
      mhtml-onunmount={ (node: org.scalajs.dom.Node) => unmountView(node) } />
  }

  private def renderEntities(renderEntity: (Identity, T) => Node): Identity => Rx[Node] = { (id: Identity) =>
    val entity: Rx[Either[Throwable, Option[T]]] =
      for {
        qp <- queryParameters
        entity <- store.read(qp)(id).toRx
      } yield entity match {
        case Some(Success(entity)) => Right(Some(entity))
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
