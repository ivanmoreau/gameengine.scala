package com.ivmoreau.game

import cats.effect.{IO, Ref}
import com.ivmoreau.game.input.Event
import com.ivmoreau.game.internal.{IOGameGlobals, Window}
import IOGameGlobals.runOnGLThread
import com.ivmoreau.game.RenderIO
import fs2.concurrent.Topic
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL

import java.util.concurrent.LinkedBlockingQueue
import java.util.stream.Stream
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, MILLISECONDS}

abstract class IGameLogic:

  val configuration: Configuration = Configuration(
    title = this.getClass.getSimpleName
  )

  def main(args: Array[String]): Unit = {
    val window: Window = new Window(configuration)
    val (ecGL, runScheduler) = scheduler()
    println("Setting up global window and execution context")
    println(s"Window: $window, ecGL: $ecGL, runScheduler: $runScheduler")
    val setGlobalWindowEc: IO[Unit] = IOGameGlobals.glctxi.set((ecGL, window))

    println("Starting game")

    IOGameGlobals.glctxi
      .set((ecGL, window))
      .flatMap { _ =>
        (for
          _ <- IO(println("Setting up OpenGL"))
          ec <-
            IOGameGlobals.glctxi.get.map(_._1)
          // is not null
          _ <- IO(println(s"ec: $ec"))
          _ <- runOnGLThread(GL.createCapabilities())
          _ <- IO(println("Setting up game"))
          state <- init()
          _ <- IO(println("Setting up game loop"))
          stateRef <- Ref.of[IO, State](state)
          _ <- gameLoop(stateRef)
        yield ())
      }
      .unsafeRunAndForget()(using cats.effect.unsafe.IORuntime.global)

    println("Running scheduler")
    runScheduler(())
  }

  private def gameLoop(initialState: Ref[IO, State]): IO[Unit] =
    val ticksPerSecond = 25
    val skipTicks = 1000 / ticksPerSecond
    val maxFrameskip = 5
    println("Starting game loop")
    for
      _ <- (runOnGLThread(GLFW.glfwPollEvents()) *> IO.sleep(
        Duration(10, MILLISECONDS)
      )).whileM_(IO(true)).start
      topic <- Topic[IO, Event]
      _ <- IOGameGlobals.glctxi.get.map(_._2).flatMap { window =>
        runOnGLThread(window.registerKeyTopic(topic))
      }
      _ <- topic
        .subscribe(10)
        .evalMap { event =>
          onEvent(event, initialState)
        }
        .compile
        .drain
        .start
      _ <- (for
        initialTime <- IO.realTime
        _ <- update(initialState)
        finalTime <- IO.realTime
        sleepTime = skipTicks - (finalTime - initialTime).toMillis
        _ <-
          if sleepTime > 0 then IO.sleep(Duration(sleepTime, MILLISECONDS))
          else IO.unit
      yield ()).whileM_(IO(true)).start
      _ <- (render(initialState) *> IO.sleep(
        Duration(5, MILLISECONDS)
      )).whileM_(IO(true)).start
    yield ()
  end gameLoop

  /** We should run this in the main thread
    * @return
    *   execution context and a function to run the scheduler
    */
  private def scheduler(): (ExecutionContext, Unit => Unit) =
    val queue: LinkedBlockingQueue[Runnable] =
      new LinkedBlockingQueue[Runnable]()
    val threadName = Thread.currentThread().getName
    if threadName != "main" then
      throw new IllegalStateException(
        s"Scheduler should be run in the main thread, but was run in $threadName"
      )
    end if

    (
      new ExecutionContext {
        def execute(runnable: Runnable): Unit =
          queue.add(runnable)
        end execute
        def reportFailure(cause: Throwable): Unit =
          cause.printStackTrace() // TODO: Do proper error handling here
        end reportFailure
      },
      _ => while queue.take().run() == () do { () }
    )
  end scheduler

  type State

  def init(): IO[State]

  def onEvent(event: Event, stateRef: Ref[IO, State]): IO[Unit]

  def update(stateRef: Ref[IO, State]): IO[Unit]

  def render(stateRef: Ref[IO, State]): IO[Unit]

  def dispose(): IO[Unit]

end IGameLogic
