package net.andimiller.cats.effect.cache
package syntax

import cats._
import cats.data._
import cats.syntax._
import cats.effect._
import net.andimiller.cats.effect.cache.KleisliCache.ResultWithExpiry

import scala.concurrent.duration.FiniteDuration

trait KleisliCacheSyntax {

  implicit class KleisliCacheOps[E[_]: Monad: Effect, I, O](t: Kleisli[E, I, O]) {
    def cache(size: Int = 1000, duration: FiniteDuration) = KleisliCache(t, size, duration)
  }

  implicit class KleisliExpiringCacheOps[E[_]: Monad: Effect, I, O](t: Kleisli[E, I, ResultWithExpiry[O]]) {
    def cache(size: Option[Int] = Some(1000)): Kleisli[E, I, O]= KleisliCache.withExpiry(t, size)
  }

}
