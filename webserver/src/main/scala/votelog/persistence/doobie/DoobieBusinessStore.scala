package votelog.persistence.doobie

import java.time.LocalDate

import cats._
import cats.data._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie._
import doobie.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Business, Language}
import votelog.persistence.BusinessStore
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta

class DoobieBusinessStore[F[_]: NonEmptyParallel: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends BusinessStore[F] {

  private def selectQuery(language: Language)(id: Business.Id): ConnectionIO[Business] =
    sql"select title, description, submitted_by, submission_date from business where id=${id} and language=${language.iso639_1}"
      .query[Business].unique

  def indexQuery(qp: IndexParameters): doobie.ConnectionIO[List[Business.Id]] =
    sql"""select id from business
         |where business_type in (3, 4, 5)
         |and submission_legislative_period = ${qp.indexContext.legislativePeriod}
         |and language = ${qp.indexContext.language.iso639_1.toUpperCase}
         |LIMIT ${qp.pageSize}
         |""".stripMargin
      .query[Business.Id]
      .accumulate[List]

  val count: doobie.ConnectionIO[Int] =
    sql"select count(id) from business".query[Int].unique

  override def read(queryParameters: ReadParameters)(id: Business.Id): F[Business] =
    selectQuery(queryParameters)(id).transact(transactor)

  override def index(indexQueryParameters: IndexParameters): F[Index[Business.Id]] = {
    val index = indexQuery(indexQueryParameters).transact(transactor)
    val count = this.count.transact(transactor)

    (count, index).parMapN(Index.apply)
  }
}
