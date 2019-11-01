package votelog

import fs2._
import cats._
import cats.effect._
import fs2.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global

object Streaming extends App {

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  val zeros = Stream(0).repeat
  val init = Stream(1,1).append(zeros)


  val bar = Stream(1).repeat.scan(1)(_ + _)
  val foo = bar.scan(0)(_ + _)

  val fibs: Stream[Pure, Int] = Stream(1) ++ qux.scan(1)(_ + _)

  val qux: Stream[Pure, Int] = Stream(0) ++ fibs.scan(2)(_ + _)

  //val r = Stream.randomSeeded(0).map(1 / _.toFloat)

  val r = Stream.randomSeeded(0).map(1 / Int.MaxValue.toDouble * _.abs.toDouble).repeat

  //println(r.take(1000).scan(0.0)(_ + _).repeat.take(2).toList)
  println(r.take(10).toList)


  def collectTens(in: Stream[Pure, Double]): Stream[Pure, Double]= {
    in.scanChunks(0.0) { case (a, chunk) =>
      val acc = chunk.foldLeft(a)( _ + _)
      if (acc > 10) (0.0, Chunk.singleton(Some(acc)))
      else (acc, Chunk.singleton(None))
    }.collect { case Some(a) => a }
  }



  //println(r.through(collectTens).take(4).toList)





  var foo1 = 1
  foo1 = 4


  val list =
    Stream(1)
      .repeat
      .scan(0)(_ + _)
      .take(10)
      .append(Stream(2).repeat.take(5))
      .toList

  val list2 = list.map(_ + 1)



}
