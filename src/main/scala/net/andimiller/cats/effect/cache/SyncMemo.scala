package net.andimiller.cats.effect.cache

import cats.effect._
import cats.effect.implicits._
import cats.effect.concurrent.Ref
import cats._, cats.data._, cats.syntax._, cats.implicits._

object SyncMemo {

  def memoize[E[_]: Sync: Monad, T](run: E[T]): E[E[T]] =
    for {
      r <- Ref.of[E, Option[T]](Option.empty[T])
    } yield
      for {
        (v, cb) <- r.access
        r1 <- if (v.isEmpty) {
               for {
                 r2      <- run
                 updated <- cb(Option(r2))
                 r3      <- if (updated) Sync[E].point(r2) else r.get.map(_.get)
               } yield r3
             } else {
               Sync[E].point(v.get)
             }
      } yield r1

}
