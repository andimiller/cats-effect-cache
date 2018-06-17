package net.andimiller.cats.effect.cache

package object syntax {
  object expireSyntax extends ExpireSyntax
  object kleisliCacheSyntax extends KleisliCacheSyntax
  object kleisliMemoSyntax extends KleisliMemoSyntax
  object all extends ExpireSyntax with KleisliCacheSyntax with KleisliMemoSyntax
}
