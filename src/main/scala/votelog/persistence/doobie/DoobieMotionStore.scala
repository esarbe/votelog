package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.politics.Motion
import votelog.persistence.MotionStore
import votelog.persistence.doobie.Mappings._
import doobie.postgres.implicits._

class DoobieMotionStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends MotionStore[F] {

  private def selectQuery(id: Motion.Id): ConnectionIO[Motion] =
    sql"select name, submitter from motions where id=${id}".query[Motion].unique

  val indexQuery: doobie.ConnectionIO[List[Motion.Id]] =
    sql"select id from motions".query[Motion.Id].accumulate[List]

  override def read(id: Motion.Id): F[Motion] =
    selectQuery(id).transact(transactor)

  override def index: F[List[Motion.Id]] =
    indexQuery.transact(transactor)

}
