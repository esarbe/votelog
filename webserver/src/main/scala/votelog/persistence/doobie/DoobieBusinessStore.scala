package votelog.persistence.doobie

import cats._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Business, Language}
import votelog.persistence.BusinessStore
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta
import votelog.domain.data.Sorting
import votelog.persistence.doobie.DoobieBusinessStore.{toFieldName, toOrderPair} // idea thinks this is not needed but it's wrong

class DoobieBusinessStore[F[_]: NonEmptyParallel: ThrowableBracket](
  transactor: doobie.util.transactor.Transactor[F]
) extends BusinessStore[F] {

  private def selectQuery(language: Language)(id: Business.Id): ConnectionIO[Business] =
    sql"select title, description, submitted_by, submission_date from business where id=${id} and language=${language.iso639_1}"
      .query[Business].unique

  def indexQuery(qp: IndexParameters): doobie.ConnectionIO[List[(Business.Id, Business.Partial)]] = {
    val orderBy = buildOrderBy(qp.orderings.filter(o => qp.fields.contains(o._1)).map((toOrderPair _).tupled))
    val fields = Business.Field.values.map {
      field =>
        qp.fields.toList.find( _ == field).map(toFieldName)
          .getOrElse(s"null as ${toFieldName(field)}")
    }

    val selectFields = buildFields(fields)

    sql"""select id $selectFields from business
         |where business_type in (3, 4, 5)
         |and submission_legislative_period = ${qp.indexContext.legislativePeriod}
         |and language = ${qp.indexContext.language.iso639_1.toUpperCase}
         |$orderBy
         |LIMIT ${qp.pageSize}
         |""".stripMargin
      .query[Business.Id]
      .map(id => (id, Business.empty))
      .accumulate[List]
  }

  val count: doobie.ConnectionIO[Int] =
    sql"select count(id) from business".query[Int].unique

  override def read(queryParameters: ReadParameters)(id: Business.Id): F[Business] =
    selectQuery(queryParameters)(id).transact(transactor)

  override def index(indexQueryParameters: IndexParameters): F[Index[Business.Id, Business.Partial]] = {
    val index = indexQuery(indexQueryParameters).transact(transactor)
    val count = this.count.transact(transactor)

    (count, index).parMapN { case (count, index) => Index[Business.Id, Business.Partial](count, index)}
  }
}

object DoobieBusinessStore {
  def toOrderPair(field: Business.Field, direction: Sorting.Direction) =
    toFieldName(field) -> direction

  val toFieldName: Business.Field => String = {
    case Business.Field.SubmissionDate => "submission_date"
    case Business.Field.SubmittedBy => "submitted_by"
    case Business.Field.Title => "title"
    case Business.Field.Description => "description"
  }
}
