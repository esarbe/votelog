package votelog.persistence.doobie

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Inside, Matchers}
import votelog.domain.politics.{Motion, Votum}
import votelog.persistence.{MotionStore, PoliticianStore, StoreSpec}

import scala.concurrent.ExecutionContext

class DoobieVoteStoreSpec extends FlatSpec
  with StoreSpec
  with ScalaFutures
  with Matchers
  with Inside {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val transactor: Transactor[IO] = TransactorBuilder.buildTransactor(getClass.getName)

  val store = new DoobieVoteStore(transactor)
  val politician = new DoobiePoliticianStore(transactor)
  val motion = new DoobieMotionStore(transactor)
  val schema = new DoobieSchema(transactor)

  schema.initialize.unsafeRunSync()

  it should "register a vote" in {

    for {
      pid1 <- politician.create(PoliticianStore.Recipe("foo"))
      pid2 <- politician.create(PoliticianStore.Recipe("bar"))
      motion1 <- motion.create(MotionStore.Recipe("foo-motion", pid1))
      _ <- store.voteFor(pid2, motion1, Votum.Abstain)
      p1votes <- store.getVotes(pid1)
      p2votes <- store.getVotes(pid2)
    } yield {
       p1votes shouldBe empty
       p2votes shouldBe List(motion1 -> Votum.Abstain)
    }
  }
}
