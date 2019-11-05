package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.politics.Motion
import votelog.persistence.MotionStore
import votelog.persistence.doobie.Mappings._
import votelog.domain.crudi.ReadOnlyStoreAlg.{IndexQueryParameters, QueryParameters}

class DoobieMotionStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends MotionStore[F] {

  private def selectQuery(qp: QueryParameters)(id: Motion.Id): ConnectionIO[Motion] =
    sql"select title, submitter from business where id=${id} and language=${qp.language}"
      .query[Motion].unique

  def indexQuery(qp: IndexQueryParameters): doobie.ConnectionIO[List[Motion.Id]] =
    sql"select id from business where business_type in (3, 4) LIMIT ${qp.pageSize}"
      .query[Motion.Id].accumulate[List]

  override def read(queryParameters: QueryParameters)(id: Motion.Id): F[Motion] =
    selectQuery(queryParameters)(id).transact(transactor)

  override def index(indexQueryParameters: IndexQueryParameters): F[List[Motion.Id]] =
    indexQuery(indexQueryParameters).transact(transactor)

}
