package votelog.endpoint

import votelog.domain.politics
import votelog.domain.politics.{Context, Language, LegislativePeriod, Person}
import votelog.orphans.endpoints4s.PersonSchema

trait PersonStoreEndpoint
  extends ReadOnlyStoreEndpoint
  with PersonSchema {

  override type Entity = Person
  override type Id = Person.Id
  override type EntityContext = Language
  override type IndexContext = Context

  val lp = qs[Int]("lp").xmap(LegislativePeriod.Id.apply)(_.value)
  val lang = qs[String]("lang").xmap(Language.fromIso639_1Unsafe)(_.iso639_1)

  override val entityContextQuery: QueryString[Language] = lang
  override val indexContextQuery: QueryString[Context] =
    (lp & lang)
      .xmap((politics.Context.apply _).tupled)(c => (c.legislativePeriod, c.language))
}
