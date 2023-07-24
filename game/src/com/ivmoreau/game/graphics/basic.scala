package com.ivmoreau.game.graphics

import cats.effect.IO
import com.ivmoreau.game.RenderIO
import org.lwjgl.opengl.GL11
import com.ivmoreau.game.internal.IOGameGlobals
import org.lwjgl.glfw.GLFW

private trait basic:

  def setClearColor(color: Color): IO[Unit] =
    IOGameGlobals.glctxi.get.flatMap { case (ecGL, window) =>
      IO {
        GL11.glClearColor(color.red, color.green, color.blue, color.alpha)
      }.evalOn(ecGL)
    }
  end setClearColor

  def clear(): IO[Unit] =
    IOGameGlobals.glctxi.get.flatMap { case (ecGL, window) =>
      IO {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)

        GLFW.glfwSwapBuffers(window.windowHandle)
      }.evalOn(ecGL)
    }
  end clear

end basic
