package votelog.endpoint.client

import endpoints.{generic, xhr}
import votelog.domain.politics.{Context, Language, LegislativePeriod, Person}
import votelog.endpoint.PersonStoreEndpoint

class PersonStoreXhrEndpoint(fragment: String)
  extends PersonStoreEndpoint
    with generic.JsonSchemas
    with endpoints.xhr.future.Endpoints
    with xhr.circe.JsonSchemaEntities
    with CorsEndpoints {

  val rootPath: Path[Unit] = staticPathSegment(fragment) / "person"

  implicit val queryStringParamLanguage: QueryStringParam[Language] = (lang: Language) => List(lang.iso639_1)
  implicit val queryStringParamLegislativePeriod: QueryStringParam[LegislativePeriod.Id] =
    (period: LegislativePeriod.Id) => List(period.value.toString)

  override implicit lazy val entityCodec: JsonSchema[Person] = genericJsonSchema[Person]
  override implicit lazy val entityIdCodec: JsonSchema[Person.Id] = genericJsonSchema[Person.Id]
  override implicit lazy val id: Segment[Person.Id] = (pid: Person.Id) => pid.value.toString

  override val contextQuery: QueryString[Context] =
    (qs[LegislativePeriod.Id]("legislativePeriod") & qs[Language]("lang"))
      .xmap(Context.tupled)(c => (c.legislativePeriod, c.language))
}
