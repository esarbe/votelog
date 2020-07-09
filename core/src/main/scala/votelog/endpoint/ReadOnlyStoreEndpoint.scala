package votelog.endpoint

import endpoints.algebra
import votelog.endpoint.ReadOnlyStoreEndpoint.Paging

trait ReadOnlyStoreEndpoint
  extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with algebra.JsonSchemas {

  type Entity
  type Id
  type IndexOptions

  type Context // language, year, ...

  implicit val entityCodec: JsonSchema[Entity]
  implicit val entityIdCodec: JsonSchema[Id]
  implicit val id: Segment[Id]

  val rootPath: Path[Unit]
  val contextQuery: QueryString[Context]

  val offsetQuery: QueryString[Long] = qs[Long]("os")
  val pageSizeQuery: QueryString[Int] = qs[Int]("ps")
  lazy val pagingQuery: QueryString[Paging] =
    (offsetQuery & pageSizeQuery).xmap(Paging.tupled)(p => (p.offset, p.pageSize))

  lazy val contextualizedPagedQuery: QueryString[(Paging, Context)] = pagingQuery & contextQuery

  lazy val idSegment = segment[Id]("id", docs = Some("Entity Id"))

  lazy val index: Endpoint[(Paging, Context), List[Id]] =
    endpoint(
      get(rootPath /? contextualizedPagedQuery),
      ok(jsonResponse[List[Id]])
    )

  lazy val read: Endpoint[(Id, Context), Option[Entity]] =
    endpoint(
      get(rootPath / idSegment /? contextQuery),
      ok(jsonResponse[Entity]).orNotFound()
    )
}

object ReadOnlyStoreEndpoint {
  case class Paging(offset: Long, pageSize: Int)
}
