package net.andimiller.cats.effect.cache
package syntax

import cats.data.Kleisli
import cats.effect.Effect

import scala.collection.concurrent.TrieMap

trait KleisliMemoSyntax {

  implicit class KleisliMemoOps[E[_]: Effect, I, O](k: Kleisli[E, I, O]) {
    def memoize(map: TrieMap[I, O] = new TrieMap[I, O]()) = KleisliMemo(k, map)
  }

}
