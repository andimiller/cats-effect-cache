package net.andimiller.cats.effect.cache.syntax

import net.andimiller.cats.effect.cache.KleisliCache.ResultWithExpiry

import scala.concurrent.duration._

trait ExpireSyntax {

  implicit class ExpireOps[T](t: T) {
    def expireAt(f: T => FiniteDuration): ResultWithExpiry[T] = ResultWithExpiry(t, f(t))

  }

}
