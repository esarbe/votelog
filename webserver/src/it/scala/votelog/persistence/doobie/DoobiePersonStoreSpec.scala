package votelog.persistence.doobie

import cats._
import cats.implicits._
import cats.effect.{ContextShift, IO}
import org.scalatest.flatspec.AnyFlatSpec
import votelog.domain.crudi.ReadOnlyStoreAlg.{Index, IndexQueryParameters}
import votelog.domain.data.Sorting
import votelog.domain.politics.{Canton, Context, Language, LegislativePeriod, Person, PersonPartial}
import doobie._
import doobie.implicits._
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import votelog.app
import votelog.app.Database.buildTransactor
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}
import votelog.domain.data.Sorting.Direction.{Ascending, Descending}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import pureconfig.generic.auto._
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta
import votelog.domain.politics.Person.Gender
import votelog.orphans.doobie.domain.politics.PersonInstances

class DoobiePersonStoreSpec extends AnyFlatSpec with Matchers with PersonInstances {

  val qp = IndexQueryParameters(
    PageSize(10),
    Offset(0),
    Context(LegislativePeriod.Id(50), Language.German),
    List[(Person.Field, Sorting.Direction)](Person.Field.LastName -> Ascending, Person.Field.FirstName -> Ascending),
    Set(Person.Field.Id, Person.Field.FirstName, Person.Field.LastName, Person.Field.DateOfBirth),
  )

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  lazy val configuration = ConfigSource.default.at("votelog").loadOrThrow[app.Configuration]

  implicit val transactor = buildTransactor[IO](configuration.curiaVista)
  val store = new DoobiePersonStore[IO](transactor)

  "DoobiePersonStore" should "retrieve index " in {
    val Index(noEntities, partialsById) = store.index(qp).unsafeRunSync()
    partialsById.size shouldBe qp.pageSize.value
    val (ids, partials) = partialsById.unzip
    partials.sortBy(p => (p.lastName, p.firstName)) shouldBe partials

  }
}
