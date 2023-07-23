package com.ivmoreau.game.internal.shaders

import com.ivmoreau.game.internal.shaders.AbstractShader
import org.lwjgl.opengl.GL20

private[game] object BasicFragmentTextureShader
    extends AbstractShader(
      glsl"""
            |#version 150 core
            |
            |in vec3 vertexColor;
            |in vec2 textureCoord;
            |
            |out vec4 fragColor;
            |
            |uniform sampler2D texImage;
            |
            |void main() {
            |    vec4 textureColor = texture(texImage, textureCoord);
            |    fragColor = vec4(vertexColor, 1.0) * textureColor;
            |}
            |""".stripMargin,
      GL20.GL_FRAGMENT_SHADER
    ):
end BasicFragmentTextureShader
