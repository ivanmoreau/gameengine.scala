package com.ivmoreau.game.graphics

import cats.effect.IO
import org.lwjgl.opengl.GL11
import com.ivmoreau.game.internal.IOGameGlobals
import org.lwjgl.glfw.GLFW

trait basic:

  def setClearColor(color: Color): IO[Unit] =
    IOGameGlobals.glctx.get.flatMap { case (ecGL, window) =>
      IO {
        GL11.glClearColor(color.red, color.green, color.blue, color.alpha)
      }.evalOn(ecGL)
    }
  end setClearColor

  def clear(): IO[Unit] =
    IOGameGlobals.glctx.get.flatMap { case (ecGL, window) =>
      IO {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)

        GLFW.glfwSwapBuffers(window)
      }.evalOn(ecGL)
    }
  end clear

end basic
