package com.ivmoreau.game.internal

package object shaders {
  implicit class GLSLOps(val sc: StringContext) extends AnyVal {

    /** For IntelliJ */
    def glsl(args: Any*): String =
      val out = sc.s(args: _*)
      out
  }
}
