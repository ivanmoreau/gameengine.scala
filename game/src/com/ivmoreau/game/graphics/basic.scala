package com.ivmoreau.game.graphics

import cats.effect.IO
import org.lwjgl.opengl.GL11
import com.ivmoreau.game.IOGameGlobals
import org.lwjgl.glfw.GLFW

trait basic:

  def setClearColor(color: Color): IO[Unit] =
    IOGameGlobals.glctx.get.flatMap { case (ecGL, window) =>
      IO {
        println(Thread.currentThread().getName())
        GL11.glClearColor(color.red, color.green, color.blue, color.alpha)
      }.evalOn(ecGL)
    }

  def clear(): IO[Unit] =
    IOGameGlobals.glctx.get.flatMap { case (ecGL, window) =>
      IO {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)

        GLFW.glfwSwapBuffers(window)
      }.evalOn(ecGL)
    }

end basic
