package com.ivmoreau.game.internal.shaders

import org.lwjgl.opengl.{GL11, GL20}

private[game] abstract class AbstractShader(
    private val shader: String,
    val shaderType: Int
):
  lazy val get: Int =
    val shaderId = GL20.glCreateShader(shaderType)
    GL20.glShaderSource(shaderId, shader)
    GL20.glCompileShader(shaderId)
    if GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE then
      val infoLog = GL20.glGetShaderInfoLog(shaderId)
      throw RuntimeException(s"Error compiling shader: $infoLog")
    end if
    shaderId
  end get
end AbstractShader
