package votelog.persistence.doobie

import cats._
import cats.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.politics.{Business, Canton, Context, Language, Person, PersonPartial}
import votelog.persistence.PersonStore
import doobie._
import doobie.implicits._
import votelog.orphans.doobie.implicits._
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta
import votelog.domain.data.Sorting // idea thinks this is not needed but it's wrong

class DoobiePersonStore[F[_]: NonEmptyParallel: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends PersonStore[F] {


  def selectQuery(id: Person.Id, lang: Language): ConnectionIO[Person] =
    sql"""
      SELECT
        p.id, p.first_name, p.last_name, canton_name, p.gender_as_string, party_name, date_election, p.date_of_birth
        FROM member_council m, person p
        WHERE m.id = p.id
        AND m.language = p.language
        AND m.id=${id.value}
        AND m.language=${lang}
      """
      .stripMargin
      .query[Person]
      .unique

  def indexQuery(p: IndexParameters): doobie.ConnectionIO[List[(Person.Id, PersonPartial)]] = {
    import shapeless._
    import doobie._
    import doobie.implicits._
    import doobie.util.Get._

    def toOrderPair(field: Person.Field, direction: Sorting.Direction) = toFieldName(field) -> direction

    val orderBy = buildOrderBy(p.orderings.filter(o => p.fields.contains(o._1)).map((toOrderPair _).tupled))
    val fields = Person.Field.values.map {
      field =>
        (p.fields ++ Set(Person.Field.Id)).toList.find( _ == field).map(toFieldName).getOrElse(s"null as ${toFieldName(field)}")
    }

    val selectFields = buildFields(fields)

    sql"""
      SELECT $selectFields from person p
      WHERE p.`language` = ${p.indexContext.language.iso639_1}
      AND p.person_number in (
        select distinct person_number
        from voting v
        where v.id_legislative_period = ${p.indexContext.legislativePeriod}
        and v.`language` = ${p.indexContext.language.iso639_1}
      )
      $orderBy
      LIMIT ${p.offset.value}, ${p.pageSize.value}
    """
      .queryWithLogHandler[PersonPartial](doobie.util.log.LogHandler.jdkLogHandler)
      .map(p => (p.id, p))
      .accumulate[List]
  }

  def count(p: IndexParameters) = {
    sql"""
      select count(distinct person_number)
      from voting v
      where v.id_legislative_period = ${p.indexContext.legislativePeriod}
      and v.`language` = ${p.indexContext.language.iso639_1}
    """.query[Int].unique
  }

  override def read(lang: Language)(id: Person.Id): F[Person] =
    selectQuery(id, lang).transact(transactor)

  override def index(p: IndexParameters): F[Index[Person.Id, PersonPartial]] = {
    (count(p), indexQuery(p)).mapN(Index.apply).transact(transactor)
  }

  val toFieldName: Person.Field => String = {
    case Person.Field.FirstName => "first_name"
    case Person.Field.LastName => "last_name"
    case Person.Field.Id => "id"
    case Person.Field.DateOfBirth => "date_of_birth"
    case Person.Field.Canton => "canton"
    case Person.Field.DateOfElection => "date_election"
    case Person.Field.Gender => "gender_as_string"
    case Person.Field.Party => "party_name"
  }

}
