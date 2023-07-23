package com.ivmoreau.game.internal.shaders

import com.ivmoreau.game.internal.shaders.AbstractShader
import org.lwjgl.opengl.GL20

private[game] object BasicVertexTextureShader
    extends AbstractShader(
      glsl"""
            |#version 330 core
            |
            |in vec2 position;
            |in vec3 color;
            |in vec2 texcoord;
            |
            |out vec3 vertexColor;
            |out vec2 textureCoord;
            |
            |uniform mat4 model;
            |uniform mat4 view;
            |uniform mat4 projection;
            |
            |void main() {
            |    vertexColor = color;
            |    textureCoord = texcoord;
            |    mat4 mvp = projection * view * model;
            |    gl_Position = mvp * vec4(position, 0.0, 1.0);
            |}
            |""".stripMargin,
      GL20.GL_VERTEX_SHADER
    ):
end BasicVertexTextureShader
