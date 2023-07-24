package com.ivmoreau.game.internal

import cats.effect.IO
import com.ivmoreau.game.Configuration
import com.ivmoreau.game.input.Event
import fs2.concurrent.Topic
import org.lwjgl.glfw.{GLFW, GLFWErrorCallback}
import org.lwjgl.opengl.GL11

class Window(configuration: Configuration) {

  def isKeyPressed(keyCode: Int): Boolean = {
    GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS
  }

  def registerKeyTopic(topic: Topic[IO, Event]): Unit =
    GLFW.glfwSetKeyCallback(
      windowHandle,
      (window, key, scancode, action, mods) => {

        Event.fromKeyAction(key, action).fold(()) { event =>
          topic
            .publish1(event)
            .unsafeRunAndForget()(using cats.effect.unsafe.implicits.global)
        }

        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
          GLFW.glfwSetWindowShouldClose(
            window,
            true
          ); // We will detect this in the rendering loop
      }
    )
  end registerKeyTopic

  // Initialization code

  GLFWErrorCallback.createPrint(System.err).set()
  if (!GLFW.glfwInit()) then
    throw new IllegalStateException("Unable to initialize GLFW")

  GLFW.glfwDefaultWindowHints()
  GLFW.glfwWindowHint(
    GLFW.GLFW_CONTEXT_VERSION_MAJOR,
    3
  )
  GLFW.glfwWindowHint(
    GLFW.GLFW_CONTEXT_VERSION_MINOR,
    2
  )
  GLFW.glfwWindowHint(
    GLFW.GLFW_OPENGL_PROFILE,
    GLFW.GLFW_OPENGL_CORE_PROFILE
  )
  GLFW.glfwWindowHint(
    GLFW.GLFW_OPENGL_FORWARD_COMPAT,
    GLFW.GLFW_TRUE
  )
  GLFW.glfwWindowHint(
    GLFW.GLFW_VISIBLE,
    GLFW.GLFW_TRUE
  )
  GLFW.glfwWindowHint(
    GLFW.GLFW_RESIZABLE,
    configuration.resizable match {
      case true  => GLFW.GLFW_TRUE
      case false => GLFW.GLFW_FALSE
    }
  )

  val windowHandle: Long = GLFW.glfwCreateWindow(
    configuration.width,
    configuration.height,
    configuration.title,
    0,
    0
  )

  if (windowHandle == 0) then
    throw new RuntimeException("Failed to create the GLFW window")
  end if

  // Setup a key callback. It will be called every time a key is pressed, repeated or released.
  /*
  GLFW.glfwSetKeyCallback(
    windowHandle,
    (window, key, scancode, action, mods) => {
      import cats.effect.unsafe.implicits.global
      windowHandle.Event.fromKeyAction(key, action).foreach { event =>
        ioEvent.publish1(event).unsafeRunSync()
      }

      if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
        GLFW.glfwSetWindowShouldClose(
          window,
          true
        ); // We will detect this in the rendering loop
    }
  );*/

  // Get the thread stack and push a new frame
  MemoryStackI.onStack { stack =>
    val pWidth = stack.mallocInt(1) // int*
    val pHeight = stack.mallocInt(1) // int*

    // Get the window size passed to glfwCreateWindow
    GLFW.glfwGetWindowSize(
      windowHandle,
      pWidth,
      pHeight
    )

    // Get the resolution of the primary monitor
    val vidmode = GLFW.glfwGetVideoMode(
      GLFW.glfwGetPrimaryMonitor()
    )

    // Center the window
    GLFW.glfwSetWindowPos(
      windowHandle,
      (vidmode.width() - pWidth.get(0)) / 2,
      (vidmode.height() - pHeight.get(0)) / 2
    )
  }

  println("Window created")

  // Make the OpenGL context current
  GLFW.glfwMakeContextCurrent(windowHandle)

  // Enable v-sync
  GLFW.glfwSwapInterval(1)

  // Setup resize callback
  GLFW.glfwSetFramebufferSizeCallback(
    windowHandle,
    (window, width, height) => {
      // Make sure the viewport matches the new window dimensions; note that width and
      // height will be significantly larger than specified on retina displays.
      GL11.glViewport(0, 0, width, height)
    }
  )

  // Make the window visible
  GLFW.glfwShowWindow(windowHandle)

  println("Window initialized")

}
