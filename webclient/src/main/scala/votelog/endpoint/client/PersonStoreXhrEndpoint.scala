package votelog.endpoint.client

import endpoints.{generic, xhr}
import votelog.domain.politics.{Context, Language, Person}
import votelog.endpoint.PersonStoreEndpoint

class PersonStoreXhrEndpoint(fragment: String)
  extends PersonStoreEndpoint
    with generic.JsonSchemas
    with endpoints.xhr.future.Endpoints
    with xhr.circe.JsonSchemaEntities {

  val rootPath: Path[Unit] = path / fragment / "person"

  implicit val queryStringParamLanguage: QueryStringParam[Language] = (lang: Language) => List(lang.iso639_1)

  override implicit lazy val entityCodec: JsonSchema[Person] = genericJsonSchema[Person]
  override implicit lazy val entityIdCodec: JsonSchema[Person.Id] = genericJsonSchema[Person.Id]
  override implicit lazy val id: Segment[Person.Id] = (pid: Person.Id) => pid.value.toString

  override val contextQuery: QueryString[Context] =
    (qs[Int]("year") & qs[Language]("lang")) .xmap(Context.tupled)(c => (c.year, c.language))
}
