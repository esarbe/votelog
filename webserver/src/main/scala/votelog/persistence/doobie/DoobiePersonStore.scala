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

  def indexQuery(p: IndexQueryParameters): doobie.ConnectionIO[List[Person.Id]] =
    sql"""
      SELECT distinct p.* from person p, voting v
      WHERE p.person_number = v.person_number
      AND v.id_legislative_period = ${p.queryParameters.legislativePeriod}
      AND p.`language` = ${p.queryParameters.language.iso639_1}
      LIMIT ${p.offset.value}, ${p.pageSize.value}
    """
      .query[Person.Id].accumulate[List]

  val count = sql"select count(id) from person".query[Int].unique

  override def read(lang: Language)(id: Person.Id): F[Person] =
    selectQuery(id, lang).transact(transactor)

  override def index(p: IndexQueryParameters): F[Index[Person.Id]] = {
    val index = indexQuery(p).transact(transactor)
    val count = this.count.transact(transactor)

    (count, index).parMapN(Index.apply)
  }
}
