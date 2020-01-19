package votelog.persistence.doobie

import cats.Monad
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.politics.Business
import votelog.persistence.BusinessStore
import votelog.orphans.doobie.implicits._

class DoobieBusinessStore[F[_]: Monad: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends BusinessStore[F] {

  private def selectQuery(qp: QueryParameters)(id: Business.Id): ConnectionIO[Business] =
    sql"select title, submitter from business where id=${id} and language=${qp.language}"
      .query[Business].unique

  def indexQuery(qp: IndexQueryParameters): doobie.ConnectionIO[List[Business.Id]] =
    sql"select id from business where business_type in (3, 4) LIMIT ${qp.pageSize}"
      .query[Business.Id].accumulate[List]

  override def read(queryParameters: QueryParameters)(id: Business.Id): F[Business] =
    selectQuery(queryParameters)(id).transact(transactor)

  override def index(indexQueryParameters: IndexQueryParameters): F[List[Business.Id]] =
    indexQuery(indexQueryParameters).transact(transactor)

}