package votelog.persistence.doobie

import cats._
import cats.data._
import cats.Monad
import cats.implicits._
import doobie._
import doobie.implicits._

import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Language, Person}
import votelog.persistence.PersonStore
import votelog.orphans.doobie.implicits._
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta

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

  def indexQuery(p: IndexParameters): doobie.ConnectionIO[List[Person.Id]] = {

    val orderBy = buildOrderBy(p.orderings.map(toFieldName))

    sql"""
      SELECT p.id from person p
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
      .queryWithLogHandler[Person.Id](LogHandler.jdkLogHandler)
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

  override def index(p: IndexParameters): F[Index[Person.Id]] = {
    (count(p), indexQuery(p)).mapN(Index.apply).transact(transactor)
  }

  val toFieldName: Person.Ordering => String = {
    case Person.Ordering.FirstName => "first_name"
    case Person.Ordering.LastName => "last_name"
    case Person.Ordering.Id => "id"
    case Person.Ordering.DateOfBirth => "date_of_birth"
  }

}
