package com.ivmoreau.game.internal

import org.lwjgl.opengl.GL11

class Renderer:

  def init(): Unit = ()

  def render(): Unit =
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
  end render

end Renderer
