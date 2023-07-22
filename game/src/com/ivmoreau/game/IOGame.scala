package com.ivmoreau.game

import cats.effect.IO
import org.lwjgl.glfw.GLFWErrorCallback

case class Configuration(
    title: String = "Game",
    width: Int = 800,
    height: Int = 480,
    resizable: Boolean = false
)

abstract class IOGame[State]:

  final def main(args: Array[String]): Unit =
    ()
  end main

  private var window: Long = 0

  private def init(): Unit =
    GLFWErrorCallback.createPrint(System.err).set()
    if (!org.lwjgl.glfw.GLFW.glfwInit()) then
      throw new IllegalStateException("Unable to initialize GLFW")

    org.lwjgl.glfw.GLFW.glfwDefaultWindowHints()
    org.lwjgl.glfw.GLFW.glfwWindowHint(
      org.lwjgl.glfw.GLFW.GLFW_VISIBLE,
      org.lwjgl.glfw.GLFW.GLFW_TRUE
    )
    org.lwjgl.glfw.GLFW.glfwWindowHint(
      org.lwjgl.glfw.GLFW.GLFW_RESIZABLE,
      configuration.resizable match {
        case true  => org.lwjgl.glfw.GLFW.GLFW_TRUE
        case false => org.lwjgl.glfw.GLFW.GLFW_FALSE
      }
    )

    window = org.lwjgl.glfw.GLFW.glfwCreateWindow(
      configuration.width,
      configuration.height,
      configuration.title,
      0,
      0
    )
    if (window == 0) then
      throw new RuntimeException("Failed to create the GLFW window")
  end init

  val configuration: Configuration = Configuration(
    title = this.getClass.getSimpleName
  )

  def create(): IO[State]
  def render(state: State): IO[Unit]
  def dispose(): IO[Unit]
  def update(state: State): IO[State]

end IOGame
