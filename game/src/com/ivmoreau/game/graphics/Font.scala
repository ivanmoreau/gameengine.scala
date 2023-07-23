package com.ivmoreau.game.graphics

import java.nio.ByteBuffer
import cats.effect.IO
import fs2.io.file.*
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap

abstract class Font private:
  val bitmap: ByteBuffer
  val cdata: ByteBuffer


end Font

object Font:
  // Reference: https://github.com/LWJGL/lwjgl3/commit/caaa82675506c7aa783e144a799e74f09d17c335#diff-787b5a4eecf9215b1ebc5d931621942377669b6325d5eb4c0a28d801e4c60c52
  def fromFile(
      pathStr: String,
      fontHeight: Int = 12,
      bitMapWidth: Int = 512,
      bitMapHeight: Int = 512
  ): IO[Font] =
    val path: Path = Path(pathStr)
    val bytes: fs2.Stream[IO, Byte] = Files[IO].readAll(path)
    val font: IO[ByteBuffer] = bytes.compile.to(Array).map(ByteBuffer.wrap)
    val bitmap: IO[ByteBuffer] = IO.blocking {
      BufferUtils.createByteBuffer(bitMapWidth * bitMapHeight)
    }
    val cdata: IO[ByteBuffer] = IO.blocking {
      BufferUtils.createByteBuffer(96 * STBTTBakedChar.SIZEOF)
    }
    val buffers: IO[(ByteBuffer, ByteBuffer)] = for
      fontBuffer <- font
      bitmapBuffer <- bitmap
      cdataBuffer <- cdata
      _ <- IO.blocking {
        stbtt_BakeFontBitmap(
          fontBuffer,
          fontHeight,
          bitmapBuffer,
          bitMapWidth,
          bitMapHeight,
          32,
          cdataBuffer.asInstanceOf
        )
      }
    yield (bitmapBuffer, cdataBuffer)

    buffers.map((buffer, cdata) =>
      new Font {
        val bitmap: ByteBuffer = buffer
        val cdata: ByteBuffer = cdata
      }
    )
  end fromFile
end Font
