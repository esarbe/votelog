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

  val contextQuery: QueryString[Context]

  val offsetQuery: QueryString[Long] = qs[Long]("offset")
  val pageSizeQuery: QueryString[Int] = qs[Int]("pageSize")
  val pagingQuery: QueryString[Paging] =
    (offsetQuery & pageSizeQuery).xmap(Paging.tupled)(p => (p.offset, p.pageSize))

  val contextualizedPagedQuery: QueryString[(Paging, Context)] =
     pagingQuery & contextQuery

  val idSegment = segment[Id]("id", docs = Some("Entity Id"))

  val index: Endpoint[(Paging, Context), List[Id]] =
    endpoint(
      get(path / "index" /? contextualizedPagedQuery),
      ok(jsonResponse[List[Id]])
    )

  val read: Endpoint[(Id, Context), Option[Entity]] =
    endpoint(
      get(path / idSegment /? contextQuery),
      ok(jsonResponse[Entity]).orNotFound()
    )
}


object ReadOnlyStoreEndpoint {
  case class Paging(offset: Long, pageSize: Int)
}
