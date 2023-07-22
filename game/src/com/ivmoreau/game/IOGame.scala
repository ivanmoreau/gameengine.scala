package com.ivmoreau.game

import cats.effect.{IO, IOLocal}
import com.ivmoreau.game.input.Event
import fs2.concurrent.Topic
import org.lwjgl.glfw.{Callbacks, GLFW, GLFWErrorCallback}
import org.lwjgl.opengl.{GL, GL11}

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Using

private object IOGameGlobals:
  val glExecutionContext: IOLocal[(ExecutionContext, Long)] = IOLocal(
    ExecutionContext.global,
    0L
  ).unsafeRunSync()(using cats.effect.unsafe.implicits.global)
end IOGameGlobals

case class Configuration(
    title: String = "Game",
    width: Int = 800,
    height: Int = 480,
    resizable: Boolean = false
)

abstract class IOGame[State]:

  final def main(args: Array[String]): Unit =
    System.out.println("Using LWJGL " + org.lwjgl.Version.getVersion + "!")
    System.out.println(
      "Using Scala stdlib " + scala.util.Properties.versionString + "!"
    )

    val ioEvent = Topic[IO, Event]
      .unsafeRunSync()(using cats.effect.unsafe.implicits.global)

    init(ioEvent)

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities

    import java.util.concurrent.ConcurrentLinkedQueue

    val queue = new ConcurrentLinkedQueue[Runnable]()

    println(Thread.currentThread().getName())
    val ecGL = ExecutionContext.fromExecutor(new Executor {
      override def execute(command: Runnable): Unit =
        queue.add(command)
    })

    println("GL Context: " + GL.getCapabilities.GL_ARB_compute_shader)

    IOGameGlobals.glExecutionContext
      .set(ecGL, window)
      .flatMap { _ =>
        println("Going to create")
        create().flatMap { state =>
          println("Going to render")
          { render(state) *> IO(println("Im redenrinf")) }
            .whileM_(IO.pure(true))
        }
      }
      .unsafeRunAndForget()(using cats.effect.unsafe.implicits.global)

    def secretLoop(): Unit = {
      val command = queue.poll()
      if (command != null) {
        command.run()
      }
      GLFW.glfwPollEvents()
      secretLoop()
    }

    secretLoop()

    // Free the window callbacks and destroy the window
    Callbacks.glfwFreeCallbacks(window)
    GLFW.glfwDestroyWindow(window)

    // Terminate GLFW and free the error callback
    GLFW.glfwTerminate()
    GLFW.glfwSetErrorCallback(null).free

    ()
  end main

  private var window: Long = 0

  private def loop(): Unit = {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities
    // Set the clear color
    GL11.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (!GLFW.glfwWindowShouldClose(window)) {
      GL11.glClear(
        GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
      ) // clear the framebuffer

      GLFW.glfwSwapBuffers(window) // swap the color buffers

      // Poll for window events. The key callback above will only be
      // invoked during this call.
      GLFW.glfwPollEvents
    }
  }

  private def init(ioEvent: Topic[IO, Event]): Unit =
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

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    GLFW.glfwSetKeyCallback(
      window,
      (window, key, scancode, action, mods) => {
        import cats.effect.unsafe.implicits.global
        input.Event.fromKeyAction(key, action).foreach { event =>
          ioEvent.publish1(event).unsafeRunSync()
        }

        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
          GLFW.glfwSetWindowShouldClose(
            window,
            true
          ); // We will detect this in the rendering loop
      }
    );

    // Get the thread stack and push a new frame
    Using.resource(
      org.lwjgl.system.MemoryStack.stackPush()
    ) { stack =>
      val pWidth = stack.mallocInt(1) // int*
      val pHeight = stack.mallocInt(1) // int*

      // Get the window size passed to glfwCreateWindow
      org.lwjgl.glfw.GLFW.glfwGetWindowSize(
        window,
        pWidth,
        pHeight
      )

      // Get the resolution of the primary monitor
      val vidmode = org.lwjgl.glfw.GLFW.glfwGetVideoMode(
        org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor()
      )

      // Center the window
      org.lwjgl.glfw.GLFW.glfwSetWindowPos(
        window,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
      )
    }

    // Make the OpenGL context current
    GLFW.glfwMakeContextCurrent(window)

    // Enable v-sync
    GLFW.glfwSwapInterval(1)

    // Make the window visible
    GLFW.glfwShowWindow(window)
  end init

  val configuration: Configuration = Configuration(
    title = this.getClass.getSimpleName
  )

  def create(): IO[State]
  def render(state: State): IO[Unit]
  def dispose(): IO[Unit]
  def update(state: State): IO[State]

end IOGame
