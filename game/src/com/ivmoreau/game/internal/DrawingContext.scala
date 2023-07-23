package com.ivmoreau.game.internal

import cats.Id
import cats.effect.kernel.Resource
import com.ivmoreau.game.internal.shaders.{
  BasicFragmentTextureShader,
  BasicVertexTextureShader,
  ShaderProgram
}
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.{GL11, GL15, GL20, GL30}
import org.lwjgl.system.MemoryUtil

import java.nio.FloatBuffer
import scala.util.Using
import scala.util.Using.Releasable
import io.github.hexagonnico.vecmatlib.matrix.Mat4f

final case class VertexArrayObject private (id: Int) extends AnyVal:
  def bind(): Unit =
    GL30.glBindVertexArray(id)

  def delete(): Unit =
    GL30.glDeleteVertexArrays(id)
end VertexArrayObject

object VertexArrayObject:
  def apply(): VertexArrayObject =
    VertexArrayObject(GL30.glGenVertexArrays())
end VertexArrayObject

final case class VertexBufferObject private (id: Int) extends AnyVal:
  def bind(target: Int): Unit =
    GL15.glBindBuffer(target, id)

  def uploadSubData(target: Int, offset: Int, data: FloatBuffer): Unit =
    GL15.glBufferSubData(target, offset, data)

  def uploadData(target: Int, size: Long, usage: Int): Unit =
    GL15.glBufferData(target, size, usage);
end VertexBufferObject

object VertexBufferObject:
  def apply(): VertexBufferObject =
    VertexBufferObject(GL15.glGenBuffers())
end VertexBufferObject

class DrawingContext private (
    var drawing: Boolean,
    var numVertices: Int,
    var vertices: FloatBuffer,
    var program: ShaderProgram,
    var vao: VertexArrayObject,
    var vbo: VertexBufferObject
):
  def flush(): Unit =
    if numVertices > 0 then
      vertices.flip()

      vao.bind()

      program.use()

      /* Upload the new vertex data */
      vbo.bind(GL15.GL_ARRAY_BUFFER)
      vbo.uploadSubData(GL15.GL_ARRAY_BUFFER, 0, vertices)

      /* Draw batch */
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numVertices)

      /* Clear vertex data for next batch */
      vertices.clear
  end flush

  def setupShaderProgram(): Unit =
    /* Generate Vertex Array Object */
    vao = VertexArrayObject()
    vao.bind()

    /* Generate Vertex Buffer Object */
    vbo = VertexBufferObject()
    vbo.bind(GL15.GL_ARRAY_BUFFER)

    /* Create FloatBuffer */
    vertices = MemoryUtil.memAllocFloat(4096)

    /* Upload null data to allocate storage for the VBO */
    val size: Long = vertices.capacity() * 32L
    vbo.uploadData(GL15.GL_ARRAY_BUFFER, size, GL15.GL_DYNAMIC_DRAW)

    /* Initialize variables */
    numVertices = 0
    drawing = false

    /* Load Shaders */
    val vertexShader = BasicVertexTextureShader
    val fragmentShader = BasicFragmentTextureShader

    /* Create Shader Program */
    program = ShaderProgram()
    program.attachShader(vertexShader)
    program.attachShader(fragmentShader)

    program.link()
    program.use()

    /* TODO: Delete linked shaders? */

    /* Get width and height of framebuffer */
    val window = GLFW.glfwGetCurrentContext()
    val (width, height) = MemoryStackI.onStack { stack =>
      val widthBuffer = stack.mallocInt(1)
      val heightBuffer = stack.mallocInt(1)
      GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer)
      (widthBuffer.get(), heightBuffer.get())
    }

    /* Specify Vertex Pointers */
    specifyVertexAttributes()

    /* Set texture uniform */
    val uniTex = program.getUniformLocation("texImage")
    program.setUniform(uniTex, 0)

    /* Set model matrix to identity matrix */
    val model = Mat4f.Identity
    val uniModel = program.getUniformLocation("model")
    program.setUniform(uniModel, model)

    /* Set view matrix to identity matrix */
    val view = Mat4f.Identity
    val uniView = program.getUniformLocation("view")
    program.setUniform(uniView, view)

    /* Set projection matrix to an orthographic projection */
    val projection = MatrixI.newOrto4f(0f, width, 0f, height, -1f, 1f)
    val uniProjection = program.getUniformLocation("projection")
    program.setUniform(uniProjection, projection)

    ()
  end setupShaderProgram

  private def specifyVertexAttributes(): Unit =
    /* Specify Vertex Pointer */
    val posAttrib = program.getAttributeLocation("position")
    program.enableVertexAttribute(posAttrib)
    program.pointVertexAttribute(posAttrib, 2, 8 * 4, 0)

    /* Specify Texture Pointer */
    val texAttrib = program.getAttributeLocation("texcoord")
    program.enableVertexAttribute(texAttrib)
    program.pointVertexAttribute(texAttrib, 2, 8 * 4, 2 * 4)

    /* Specify Color Pointer */
    val colAttrib = program.getAttributeLocation("color")
    program.enableVertexAttribute(colAttrib)
    program.pointVertexAttribute(colAttrib, 4, 8 * 4, 4 * 4)
  end specifyVertexAttributes

  /** Hack */
  private class release extends Releasable[Unit]:
    private def cb(): Unit =
      drawing = false
      flush()
    end cb
    def release(resource: Unit): Unit = cb()
  end release

  def withAutoRelease[A](f: => A): A =
    Using.resource {
      if drawing then
        throw new IllegalStateException("Drawing context already in use")
      drawing = true
      numVertices = 0
    }(_ => f)(using new release)
  end withAutoRelease
end DrawingContext
