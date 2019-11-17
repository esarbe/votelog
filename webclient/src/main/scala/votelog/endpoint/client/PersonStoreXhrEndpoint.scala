package votelog.endpoint.client

import endpoints.{generic, xhr}
import votelog.domain.politics.{Context, Language, Person}
import votelog.endpoint.PersonStoreEndpoint

object PersonStoreXhrEndpoint
  extends PersonStoreEndpoint
    with xhr.thenable.Endpoints
    with generic.JsonSchemas
    with xhr.circe.JsonSchemaEntities { self =>

  implicit val queryStringParamLanguage: QueryStringParam[Language] = (lang: Language) => List(lang.iso639_1)

  override implicit val entityCodec: JsonSchema[Person] = genericJsonSchema[Person]
  override implicit val entityIdCodec: JsonSchema[Person.Id] = genericJsonSchema[Person.Id]
  override implicit val id: PersonStoreXhrEndpoint.Segment[Person.Id] = (pid: Person.Id) => pid.value.toString
  override val contextQuery: PersonStoreXhrEndpoint.QueryString[Context] =
    (qs[Int]("year") & qs[Language]("lang")) .xmap(Context.tupled)(c => (c.year, c.language))

}
