package com.ivmoreau.game.internal

import cats.effect.IOLocal

import scala.concurrent.ExecutionContext

private[game] object IOGameGlobals:
  val glctx: IOLocal[(ExecutionContext, Long)] = IOLocal(
    ExecutionContext.global,
    0L
  ).unsafeRunSync()(using cats.effect.unsafe.implicits.global)
end IOGameGlobals
