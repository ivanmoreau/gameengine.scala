package com.ivmoreau.game

import cats.effect.{IO, IOLocal, Ref}
import com.ivmoreau.game.input.Event
import fs2.concurrent.Topic
import org.lwjgl.glfw.{Callbacks, GLFW, GLFWErrorCallback}
import org.lwjgl.opengl.{GL, GL11}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Using

private object IOGameGlobals:
  val glctx: IOLocal[(ExecutionContext, Long)] = IOLocal(
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

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  final def main(args: Array[String]): Unit =

    (logger.info("Starting game") *>
      logger.info("Using LWJGL " + org.lwjgl.Version.getVersion + "!") *>
      logger.info(
        "Using Scala stdlib " + scala.util.Properties.versionString + "!"
      ) *> logger.info("Initializing GLFW..."))
      .unsafeRunSync()(using cats.effect.unsafe.implicits.global)

    val ioEvent = Topic[IO, Event]
      .unsafeRunSync()(using cats.effect.unsafe.implicits.global)

    init(ioEvent)

    logger
      .info("GLFW initialized")
      .unsafeRunSync()(using cats.effect.unsafe.implicits.global)

    GL.createCapabilities

    import java.util.concurrent.ConcurrentLinkedQueue

    val queue = new ConcurrentLinkedQueue[Runnable]()

    println(Thread.currentThread().getName())

    val ecGL = ExecutionContext.fromExecutor(new Executor {
      override def execute(command: Runnable): Unit =
        queue.add(command)
    })

    val stateRef: Ref[IO, State] = Ref.unsafe[IO, State](null.asInstanceOf)

    // Setting up the game and then executing the render loop
    (logger.info("Setting internal execution context for LWJGL...") *>
      IOGameGlobals.glctx
        .set(ecGL, window)
        .flatMap { _ =>
          logger.info("Creating...") *>
            create().flatMap { state =>
              logger.info("Game initialized") *>
                stateRef.set(state) *>
                logger.info("Executing render loop...") *>
                stateRef.get
                  .flatMap(render)
                  .whileM_(IO.pure(true))
            }
        }
        .start).unsafeRunAndForget()(using cats.effect.unsafe.implicits.global)

    // Setting up the event listener stream
    IOGameGlobals.glctx
      .set(ecGL, window)
      .flatMap { _ =>
        logger.info("Executing IO event listener...") *>
          ioEvent
            .subscribe(1)
            .evalMap { event =>
              logger.trace(s"Received event $event") *>
                onEvent(event)(stateRef)
            }
            .compile
            .drain
      }
      .start
      .unsafeRunAndForget()(using cats.effect.unsafe.implicits.global)

    // Setting up the update loop
    IOGameGlobals.glctx
      .set(ecGL, window)
      .flatMap { _ =>
        logger.info("Executing update loop...") *>
          update(stateRef)
            .whileM_(IO.pure(true))
      }
      .start
      .unsafeRunAndForget()(using cats.effect.unsafe.implicits.global)

    @tailrec
    def secretLoop(): Unit =
      val command = queue.poll()
      if (command != null) {
        command.run()
      }
      GLFW.glfwPollEvents()

      if (!GLFW.glfwWindowShouldClose(window))
        secretLoop()
    end secretLoop

    secretLoop()

    // Free the window callbacks and destroy the window
    Callbacks.glfwFreeCallbacks(window)
    GLFW.glfwDestroyWindow(window)

    // Terminate GLFW and free the error callback
    GLFW.glfwTerminate()
    GLFW.glfwSetErrorCallback(null).free
  end main

  private var window: Long = 0

  private def init(ioEvent: Topic[IO, Event]): Unit =
    GLFWErrorCallback.createPrint(System.err).set()
    if (!GLFW.glfwInit()) then
      throw new IllegalStateException("Unable to initialize GLFW")

    GLFW.glfwDefaultWindowHints()
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

    window = GLFW.glfwCreateWindow(
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
  def onEvent(event: Event)(state: Ref[IO, State]): IO[Unit]
  def update(state: Ref[IO, State]): IO[Unit]
  def dispose(): IO[Unit]

end IOGame
