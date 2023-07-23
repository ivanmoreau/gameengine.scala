package com.ivmoreau.game.internal

import io.github.hexagonnico.vecmatlib.vector.VecFloat

import java.nio.FloatBuffer
import io.github.hexagonnico.vecmatlib.matrix.Mat4f
import io.github.hexagonnico.vecmatlib.matrix.MatFloat
import io.github.hexagonnico.vecmatlib.matrix.Mat3f
import io.github.hexagonnico.vecmatlib.matrix.Mat2f

object VectorI:
  def toBuffer[V <: VecFloat[V]](
      buffer: FloatBuffer
  )(vecFloat: VecFloat[V]): Unit = {
    vecFloat.toArray.foreach(buffer.put)
  }
end VectorI


object MatrixI:
  def newOrto4f(
      left: Float,
      right: Float,
      bottom: Float,
      top: Float,
      near: Float,
      far: Float
  ): Mat4f =
    val matrix = Mat4f.Zero
    val tx = -(right + left) / (right - left)
    val ty = -(top + bottom) / (top - bottom)
    val tz = -(far + near) / (far - near)

    matrix.copy(
      m00 = 2f / (right - left),
      m11 = 2f / (top - bottom),
      m22 = -2f / (far - near),
      m30 = tx,
      m31 = ty,
      m32 = tz,
    )

  def toBuffer
      (buffer: FloatBuffer)
      (matFloat: Mat4f): Unit =
    matFloat.col0.toArray.foreach(buffer.put)
    matFloat.col1.toArray.foreach(buffer.put)
    matFloat.col2.toArray.foreach(buffer.put)
    matFloat.col3.toArray.foreach(buffer.put)

  def toBuffer
      (buffer: FloatBuffer)
      (matFloat: Mat3f): Unit =
    matFloat.col0.toArray.foreach(buffer.put)
    matFloat.col1.toArray.foreach(buffer.put)
    matFloat.col2.toArray.foreach(buffer.put)

  def toBuffer
      (buffer: FloatBuffer)
      (matFloat: Mat2f): Unit =
    matFloat.col0.toArray.foreach(buffer.put)
    matFloat.col1.toArray.foreach(buffer.put)
end MatrixI