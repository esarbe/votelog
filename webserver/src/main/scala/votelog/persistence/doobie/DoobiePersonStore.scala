package votelog.persistence.doobie

import cats._
import cats.data._
import cats.Monad
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.domain.politics.Person
import votelog.persistence.PersonStore
import votelog.orphans.doobie.implicits._

class DoobiePersonStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends PersonStore[F] {

  def selectQuery(id: Person.Id): ConnectionIO[Person] =
    sql"""
      SELECT
        id, first_name, last_name, canton_name, gender_as_string, party_name, date_election, date_of_birth
        FROM member_council m, person p
        WHERE m.id = p.id
        AND m.language = p.language
        AND m.id=${id.value}
      """
      .stripMargin
      .query[Person]
      .unique

  def indexQuery(p: IndexQueryParameters): doobie.ConnectionIO[List[Person.Id]] =
    sql"""
      SELECT id
        FROM person
        WHERE language=${p.queryParameters.language.iso639_1}
        LIMIT ${p.offset.value}, ${p.pageSize.value}
      """
      .query[Person.Id].accumulate[List]

  override def read(p: QueryParameters)(id: Person.Id): F[Person] =
    selectQuery(id).transact(transactor)

  override def index(p: IndexQueryParameters): F[List[Person.Id]] =
    indexQuery(p).transact(transactor)
}
