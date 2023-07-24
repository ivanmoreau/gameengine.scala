package com.ivmoreau.game

import cats.effect.IO
import cats.effect.kernel.Async

object RenderIO:
  opaque type RenderIOInternalType[+A] = IO[A]
  type RenderIO[+A] = RenderIOInternalType[A] & IO[A]

  private[game] def toRenderIO[A](io: IO[A]): RenderIO[A] =
    io

  def apply[A](a: => A): RenderIO[A] =
    IO(a)

  given asyncForRenderIO: Async[RenderIO] = IO.asyncForIO
end RenderIO

export RenderIO.RenderIO
