package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.log.LogHandler
import votelog.domain.politics.Politician
import votelog.persistence.PoliticianStore
import votelog.persistence.doobie.Mappings._

class DoobiePoliticianStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends PoliticianStore[F] {

  def selectQuery(id: Politician.Id): ConnectionIO[Politician] =
    sql"select name from politicians where id=${id}"
      .query[Politician]
      .unique

  val indexQuery: doobie.ConnectionIO[List[Politician.Id]] =
    sql"select id from politicians".query[Politician.Id].accumulate[List]

  override def read(id: Politician.Id): F[Politician] =
    selectQuery(id).transact(transactor)

  override def index: F[List[Politician.Id]] =
    indexQuery.transact(transactor)
}
