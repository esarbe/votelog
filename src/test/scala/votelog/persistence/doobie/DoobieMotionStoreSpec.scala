package votelog.persistence.doobie


import cats._
import cats.implicits._
import cats.effect.{Async, ContextShift, IO}
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import doobie.implicits._
import doobie._
import doobie.scalatest._
import doobie.util.transactor.Transactor.Aux
import org.scalatest.concurrent.ScalaFutures
import votelog.domain.authorization.User
import votelog.domain.politics.{Motion, Politician}
import votelog.persistence.{MotionStore, PoliticianStore}
import votelog.persistence.doobie.DoobieMotionStoreSpec.Scope

import scala.concurrent.ExecutionContext

class DoobieMotionStoreSpec extends WordSpec with ScalaFutures with Matchers {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  "DoobieMotionStoreSpec" should {

    "read" in new Scope {

      val entity =
        for {
          recipe <- recipe
          id <- store.create(recipe)
          entity <- store.read(id)
        } yield entity

      entity.unsafeRunSync() shouldBe Motion(Motion.Id(1),"foo-motion", Politician.Id(1))
    }

    "update" in {

    }

    "index" in {

    }

    "delete" in {

    }

    "create" in {

    }

  }
}

object DoobieMotionStoreSpec {
  class Scope(implicit cs: ContextShift[IO]){

    val transactor =
      Transactor.fromDriverManager[IO](
        "org.h2.Driver",
        "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "sa",
        "",
      )

    val store = new DoobieMotionStore(transactor)

    val recipe: IO[MotionStore.Recipe] = {
      val schema = new DoobieSchema(transactor)
      val politician = new DoobiePoliticianStore(transactor)

      for {
        _ <- schema.initialize
        pid <- politician.create(PoliticianStore.Recipe("foo"))
      } yield MotionStore.Recipe("foo-motion", pid)
    }
  }
}