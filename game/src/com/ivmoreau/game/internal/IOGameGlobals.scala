package com.ivmoreau.game.internal

import cats.effect.{IO, IOLocal}

import scala.concurrent.ExecutionContext

private[game] object IOGameGlobals:
  val glctx: IOLocal[(ExecutionContext, Long)] = IOLocal(
    null,
    0L
  ).unsafeRunSync()(using cats.effect.unsafe.implicits.global)

  val glctxi: IOLocal[(ExecutionContext, Window)] = IOLocal(
    null,
    null
  ).unsafeRunSync()(using cats.effect.unsafe.implicits.global)

  def runOnGLThread[A](f: => A): IO[A] =
    for
      ec <- glctxi.get.map(_._1)
      _ <- IO { assert(ec ne null) }
      a <- IO { f }.evalOn(ec)
    yield a
  end runOnGLThread
end IOGameGlobals
