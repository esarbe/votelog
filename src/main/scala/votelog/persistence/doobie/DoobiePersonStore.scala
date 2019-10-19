package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.politics.Person
import votelog.infrastructure.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}
import votelog.persistence.PersonStore
import votelog.persistence.doobie.Mappings._

class DoobiePersonStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends PersonStore[F] {

  def selectQuery(id: Person.Id): ConnectionIO[Person] =
    sql"select name from person where id=${id}"
      .query[Person]
      .unique

  val indexQuery: doobie.ConnectionIO[List[Person.Id]] =
    sql"select id from person".query[Person.Id].accumulate[List]

  override def read(p: QueryParameters)(id: Person.Id): F[Person] =
    selectQuery(id).transact(transactor)

  override def index(p: IndexQueryParameters): F[List[Person.Id]] =
    indexQuery.transact(transactor)
}
