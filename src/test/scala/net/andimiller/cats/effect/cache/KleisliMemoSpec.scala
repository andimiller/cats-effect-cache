package net.andimiller.cats.effect.cache

import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{FlatSpec, MustMatchers}
import cats.data._
import cats._
import cats.syntax._
import cats.implicits._
import cats.effect._
import cats.effect.syntax._
import cats.effect.implicits._

import scala.collection.concurrent.TrieMap

class KleisliMemoSpec extends FlatSpec with MustMatchers {

  "memoizing a task" should "cause it's body to only be.unsafeRunSync once if.unsafeRunSync multiple times with the same input" in {

    @volatile var counter = 0

    val f = Kleisli { (input: Int) =>
        IO {
          println(s"doubling $input")
          counter += 1
          input * 2
        }
      }

    val func = KleisliMemo(f)

    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(3).unsafeRunSync must equal(6)
    counter must equal(2)
    func(2).unsafeRunSync must equal(4)
    counter must equal(2)
  }

  "memoizing a task using the helper" should "cause it's body to only be.unsafeRunSync once if.unsafeRunSync multiple times with the same input" in {

    @volatile var counter = 0

    import KleisliMemo._

    val f = Kleisli[IO, Int, Int] { (input: Int) =>
      IO {
        println(s"doubling $input")
        counter += 1
        input * 2
      }
    }

    val func = KleisliMemo(f)

    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(2).unsafeRunSync must equal(4)
    counter must equal(1)
    func(3).unsafeRunSync must equal(6)
    counter must equal(2)
    func(2).unsafeRunSync must equal(4)
    counter must equal(2)
  }

  "memoizing a task which can fail" should "only save the successful attempts" in {

    var counters = new TrieMap[Int, AtomicInteger]()

    val func = Kleisli[IO, Int, Int] { (input: Int) =>
      IO[Int] {
        val c     = counters.getOrElseUpdate(input, new AtomicInteger(0))
        val value = c.incrementAndGet()
        assert(value == 2)
        value
      }
    }

    val map      = new TrieMap[Int, Int]()
    val memoized = KleisliMemo(func, map)

    memoized(0).attempt.unsafeRunSync.isLeft must equal(true)
    memoized(0).attempt.unsafeRunSync.toOption.get must equal(2)
    memoized(0).attempt.unsafeRunSync.toOption.get must equal(2)
    memoized(0).attempt.unsafeRunSync.toOption.get must equal(2)

    val expectedmap = new TrieMap[Int, Int]()
    expectedmap.put(0, 2)
    map must equal(expectedmap)
  }

  "caching a task which can fail" should "cache it during the time period stated" in {
    import scala.concurrent.duration._
    import KleisliCache._

    var counters = new TrieMap[Int, AtomicInteger]()

    val f = Kleisli[IO, Int, ResultWithExpiry[Int]] { (input: Int) =>
      IO[ResultWithExpiry[Int]] {
        val c     = counters.getOrElseUpdate(input, new AtomicInteger(0))
        val value = c.incrementAndGet()
        ResultWithExpiry(value, 2 seconds)
      }
    }

    val cached = KleisliCache.withExpiry(f)

    cached(0).attempt.unsafeRunSync.toOption.get must equal(1)
    cached(0).attempt.unsafeRunSync.toOption.get must equal(1)
    cached(0).attempt.unsafeRunSync.toOption.get must equal(1)

    Thread.sleep(3000)

    cached(0).attempt.unsafeRunSync.toOption.get must equal(2)

    counters.get(0).get.get must equal(2)
  }

  "sequence test" should "work" in {
    @volatile var counter = 0

    import KleisliMemo._

    val f = Kleisli[IO, Int, Int] { (input: Int) =>
      IO {
        println(s"doubling $input")
        counter += 1
        input * 2
      }
    }

    val func = KleisliMemo(f)

    val s: List[IO[Int]] = List(func(2), func(2), func(2))

    val ec = scala.concurrent.ExecutionContext.global
    val r: IO[List[Int]] = s.sequence

    r.unsafeRunSync must equal(List(4,4,4))
    counter must equal(1)
  }

  "parSequence test" should "work" in {
    @volatile var counter = 0

    import KleisliMemo._

    val f = Kleisli[IO, Int, Int] { (input: Int) =>
      IO {
        println(s"doubling $input")
        counter += 1
        input * 2
      }
    }

    val func = KleisliMemo(f)

    val s: List[IO[Int]] = List(func(2), func(2), func(2))

    val ec = scala.concurrent.ExecutionContext.global
    implicit val t = IO.timer(ec)
    implicit val p = IO.ioParallel
    val r: IO[List[Int]] = s.map(IO.shift(ec) *> _).parSequence

    r.unsafeRunSync must equal(List(4,4,4))
  }

}
