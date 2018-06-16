package net.andimiller.cats.effect.cache

import cats._
import cats.data._
import cats.syntax._
import cats.effect._
import cats.effect.syntax._
import cats.effect.concurrent._
import scala.collection.concurrent.TrieMap

object KleisliMemo {

  /**
    * Memoize a Kleisli over an Effect, using an internal TrieMap
    * @param k  keleisli to memoize
    * @tparam E
    * @tparam I
    * @tparam O
    * @return
    */
  def apply[E[_]: Monad, I, O](k: Kleisli[E, I, O],
                               t: TrieMap[I, O] = TrieMap[I, O]())(
      implicit E: Effect[E]): Kleisli[E, I, O] = {
    Kleisli { i: I =>
      OptionT(
        E.delay {
          t.get(i)
        }
      ).getOrElseF(
        E.flatTap(k.run(i)) { r =>
          E.delay {
            t.putIfAbsent(i, r)
          }
        }
      )
    }
  }

}
