package com.ivmoreau.game.internal

import cats.effect.IO
import cats.effect.kernel.Resource
import org.lwjgl.system.MemoryStack as MS

import scala.util.Using

private[game] object MemoryStackI:
  /** HIGHLY UNSAFE, BE CAREFUL TO NOT LEAK MEMORY */
  def onStack[A](f: MS => A): A =
    Using.resource(MS.stackPush())(f)
end MemoryStackI
