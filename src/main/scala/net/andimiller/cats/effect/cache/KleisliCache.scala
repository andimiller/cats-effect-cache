package net.andimiller.cats.effect.cache

import java.{lang, util}
import java.util.concurrent.{Callable, ConcurrentMap, TimeUnit}

import cats._
import cats.data._
import cats.syntax._
import cats.effect._
import cats.effect.syntax._
import cats.effect.concurrent._
import com.github.benmanes.caffeine.cache._

import scala.concurrent.duration._
import scala.language.postfixOps

object KleisliCache {

  def apply[E[_], I, O](k: Kleisli[E, I, O],
                        size: Int = 1000,
                        expiry: Duration = 10 minutes)(
      implicit E: Effect[E]): Kleisli[E, I, O] = {
    val cache: LoadingCache[I, O] = Caffeine
      .newBuilder()
      .initialCapacity(size)
      .expireAfterWrite(expiry.toSeconds, TimeUnit.SECONDS)
      .asInstanceOf[Caffeine[I, O]]
      .build { i: I =>
        E.toIO(k.run(i)).unsafeRunSync()
      }

    Kleisli { i: I =>
      E.delay {
        cache.get(i)
      }
    }

  }

  case class ResultWithExpiry[R](value: R, expiry: FiniteDuration)

  def withExpiry[E[_], I, O](k: Kleisli[E, I, ResultWithExpiry[O]],
                             size: Option[Int] = Some(1000))(
      implicit E: Effect[E]): Kleisli[E, I, O] = {

    val cache: LoadingCache[I, ResultWithExpiry[O]] = {
      size match {
        case Some(s) => Caffeine.newBuilder().maximumSize(s)
        case None    => Caffeine.newBuilder()
      }
    }.expireAfter(new Expiry[I, ResultWithExpiry[O]] {
        override def expireAfterCreate(key: I,
                                       value: ResultWithExpiry[O],
                                       currentTime: Long): Long = {
          value.expiry.toNanos
        }
        override def expireAfterUpdate(key: I,
                                       value: ResultWithExpiry[O],
                                       currentTime: Long,
                                       currentDuration: Long): Long =
          currentDuration
        override def expireAfterRead(key: I,
                                     value: ResultWithExpiry[O],
                                     currentTime: Long,
                                     currentDuration: Long): Long =
          currentDuration
      })
      .asInstanceOf[Caffeine[I, ResultWithExpiry[O]]]
      .build { i: I =>
        E.toIO(k.run(i)).unsafeRunSync()
      }

    Kleisli { i: I =>
      E.delay {
        cache.get(i).value
      }
    }
  }

}
