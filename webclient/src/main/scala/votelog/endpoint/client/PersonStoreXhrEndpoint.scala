package votelog.endpoint.client

import endpoints.{algebra, generic, xhr}
import votelog.domain.politics.{Context, Language, Person}
import votelog.endpoint.PersonStoreEndpoint
import votelog.endpoint.ReadOnlyStoreEndpoint.Paging

class PersonStoreXhrEndpoint
  extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with generic.JsonSchemas
    with xhr.future.Endpoints
    with xhr.circe.JsonSchemaEntities {

  val offsetQuery: QueryString[Long] = qs[Long]("offset")
  val pageSizeQuery: QueryString[Int] = qs[Int]("pageSize")
  lazy val pagingQuery: QueryString[Paging] =
    (offsetQuery & pageSizeQuery).xmap(Paging.tupled)(p => (p.offset, p.pageSize))

  lazy val contextualizedPagedQuery: QueryString[(Paging, Context)] =
    pagingQuery & contextQuery

  lazy val idSegment = segment[Person.Id]("id", docs = Some("Entity Id"))

  lazy val index: Endpoint[(Paging, Context), List[Person.Id]] =
    endpoint(
      get(path / "index" /? contextualizedPagedQuery),
      ok(jsonResponse[List[Person.Id]])
    )

  lazy val read: Endpoint[(Person.Id, Context), Option[Person]] =
    endpoint(
      get(path / idSegment /? contextQuery),
      ok(jsonResponse[Person]).orNotFound()
    )


  implicit val queryStringParamLanguage: QueryStringParam[Language] = (lang: Language) => List(lang.iso639_1)

  implicit lazy val entityCodec: JsonSchema[Person] = genericJsonSchema[Person]
  implicit lazy val entityIdCodec: JsonSchema[Person.Id] = genericJsonSchema[Person.Id]
  implicit lazy val id: Segment[Person.Id] = (pid: Person.Id) => pid.value.toString
  lazy val contextQuery: QueryString[Context] =
    (qs[Int]("year") & qs[Language]("lang")) .xmap(Context.tupled)(c => (c.year, c.language))

}
