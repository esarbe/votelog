package votelog.endpoint

import endpoints4s.algebra
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

trait ReadOnlyStoreEndpoint
  extends UserAuthentication
    with algebra.JsonEntitiesFromSchemas
    with algebra.JsonSchemas {

  type Entity
  type Id
  type IndexOptions

  type EntityContext // query context for entity
  type IndexContext // query context for index

  implicit val entityCodec: JsonSchema[Entity]
  implicit val entityIdCodec: JsonSchema[Id]
  implicit def indexCodec[T]: JsonSchema[Index[T]]
  implicit val id: Segment[Id]

  val rootPath: Path[Unit]
  val entityContextQuery: QueryString[EntityContext]
  val indexContextQuery: QueryString[IndexContext]

  val offsetQuery: QueryString[Offset] = qs[Long]("os").xmap(Offset.apply)(_.value)
  val pageSizeQuery: QueryString[PageSize] = qs[Int]("ps").xmap(PageSize.apply)(_.value)
  lazy val pagingQuery: QueryString[(Offset, PageSize)] = offsetQuery & pageSizeQuery

  lazy val contextualizedPagedQuery: QueryString[(Offset, PageSize, IndexContext)] = pagingQuery & indexContextQuery

  lazy val idSegment = segment[Id]("id", docs = Some("Entity Id"))

  lazy val index: Endpoint[(Offset, PageSize, IndexContext, AuthenticationToken), Index[Id]] =
    authenticatedEndpoint(
      Get,
      rootPath /? contextualizedPagedQuery,
      emptyRequest,
      ok(jsonResponse[Index[Id]])
    )

  lazy val read: Endpoint[(Id, EntityContext, AuthenticationToken), Option[Entity]] =
    authenticatedEndpoint(
      Get,
      rootPath / idSegment /? entityContextQuery,
      emptyRequest,
      ok(jsonResponse[Entity]).orNotFound()
    )

}
