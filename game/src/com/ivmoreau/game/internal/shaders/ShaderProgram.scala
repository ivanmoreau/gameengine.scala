package com.ivmoreau.game.internal.shaders

import com.badlogic.gdx.graphics.GL30
import com.ivmoreau.game.internal.{MemoryStackI, VectorI}
import io.github.hexagonnico.vecmatlib.vector.{Vec3f, VecFloat}
import org.lwjgl.opengl.{GL11, GL15, GL20, GL21, GL31, GL32, GL33}
import io.github.hexagonnico.vecmatlib.matrix.Mat4f
import com.ivmoreau.game.internal.MatrixI
import io.github.hexagonnico.vecmatlib.matrix.Mat3f
import io.github.hexagonnico.vecmatlib.matrix.Mat2f

class ShaderProgram(id: Int):
  def attachShader(shader: AbstractShader): Unit =
    GL20.glAttachShader(id, shader.get)

  def use(): Unit =
    GL20.glUseProgram(id)

  def link(): Unit =
    GL20.glLinkProgram(id)

  def getAttributeLocation(name: String): Int =
    GL20.glGetAttribLocation(id, name)

  def enableVertexAttribute(location: Int): Unit =
    GL20.glEnableVertexAttribArray(location)

  def disableVertexAttribute(location: Int): Unit =
    GL20.glDisableVertexAttribArray(location)

  def pointVertexAttribute(
      location: Int,
      size: Int,
      stride: Int,
      offset: Int
  ): Unit =
    GL20.glVertexAttribPointer(
      location,
      size,
      GL11.GL_FLOAT,
      false,
      stride,
      offset
    )

  def getUniformLocation(name: String): Int =
    GL20.glGetUniformLocation(id, name)

  def setUniform(location: Int, value: Int): Unit =
    GL20.glUniform1i(location, value)

  def setUniform[V <: VecFloat[V]](location: Int, value: VecFloat[V]): Unit =
    MemoryStackI.onStack { stack =>
      val buffer = stack.mallocFloat(2)
      VectorI.toBuffer(buffer)(value)
      buffer.flip()
      GL20.glUniform2fv(location, buffer)
    }

  def setUniform(location: Int, value: Mat4f): Unit =
    MemoryStackI.onStack { stack =>
      val buffer = stack.mallocFloat(1)
      MatrixI.toBuffer(buffer)(value)
      buffer.flip()
      GL20.glUniform1fv(location, buffer)
    }

  def setUniform(location: Int, value: Mat3f): Unit =
    MemoryStackI.onStack { stack =>
      val buffer = stack.mallocFloat(1)
      MatrixI.toBuffer(buffer)(value)
      buffer.flip()
      GL20.glUniform1fv(location, buffer)
    }

  def setUniform(location: Int, value: Mat2f): Unit =
    MemoryStackI.onStack { stack =>
      val buffer = stack.mallocFloat(1)
      MatrixI.toBuffer(buffer)(value)
      buffer.flip()
      GL20.glUniform1fv(location, buffer)
    }

end ShaderProgram

object ShaderProgram:
  def apply(): ShaderProgram =
    val id = GL20.glCreateProgram()
    new ShaderProgram(id)
end ShaderProgram
