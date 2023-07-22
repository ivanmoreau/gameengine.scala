package com.ivmoreau.game

import cats.effect.std.{CountDownLatch, Dispatcher}
import cats.effect.unsafe.IORuntime
import cats.effect.{IO, IOApp, Resource}
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import com.badlogic.gdx.backends.lwjgl3.{
  Lwjgl3Application,
  Lwjgl3ApplicationConfiguration
}
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.GL20
import fs2.concurrent.Topic

case class GameTT() extends ApplicationAdapter:

  var batch: SpriteBatch = null
  var font: BitmapFont = null

  override def create(): Unit =
    batch = new SpriteBatch()
    font = new BitmapFont()

  override def render(): Unit =
    Gdx.gl.glClearColor(.25f, .25f, .25f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.begin()
    font.draw(
      batch,
      "Holi! uwu",
      Gdx.graphics.getWidth() / 2,
      Gdx.graphics.getHeight() / 2
    )
    batch.end()

  override def dispose(): Unit =
    batch.dispose()
    font.dispose()

end GameTT

trait IOAppGame:
  private class ImpureGame(
      createTopic: Topic[IO, Unit],
      renderTopic: Topic[IO, Unit],
      disposeTopic: Topic[IO, Unit]
  )(readyReleaser: CountDownLatch[IO])(using IORuntime)
      extends ApplicationAdapter:

    def newSpriteBatch: Resource[IO, Resource[IO, Unit]] =
      val spriteBatch = new SpriteBatch()
      Resource.make {
        IO {
          Resource.make(IO.blocking(spriteBatch.begin()))(_ =>
            IO.blocking(spriteBatch.end())
          )
        }
      }(_ => IO.blocking(spriteBatch.dispose()))

    override def create(): Unit =
      (IO.blocking(println("relese c")) *>
        createTopic.publish1(()) *> IO.blocking(
          println("publish c")
        ) *> readyReleaser.release).unsafeRunSync()

    override def render(): Unit =
      renderTopic.publish1(()).unsafeRunSync()

    override def dispose(): Unit =
      disposeTopic.publish1(()).unsafeRunSync()
  end ImpureGame

  private val createTopic: IO[Topic[IO, Unit]] = Topic[IO, Unit]
  private val renderTopic: IO[Topic[IO, Unit]] = Topic[IO, Unit]
  private val disposeTopic: IO[Topic[IO, Unit]] = Topic[IO, Unit]

  private def pipeline(
      waiter: CountDownLatch[IO]
  ): IO[(Topic[IO, Unit], Topic[IO, Unit], Topic[IO, Unit])] =
    for
      create <- createTopic
      render <- renderTopic
      dispose <- disposeTopic
      _ <- (for
        _ <- IO.blocking(println("await"))
        state <- IO.ref(game.initialState)
        _ <- create
          .subscribe(1)
          .evalMap(_ => waiter.release *> game.create())
          .compile
          .drain *> IO
          .blocking(println("create"))
        _ <- render
          .subscribe(1)
          .evalMap(_ => state.get.flatMap(game.render(_)))
          .compile
          .drain
          .start &> dispose
          .subscribe(1)
          .evalMap(_ => game.dispose())
          .compile
          .drain
          .start
      yield ()).start
    yield (create, render, dispose)

  final def main(args: Array[String]): Unit =
    import cats.effect.unsafe.implicits.global
    val cfg = new Lwjgl3ApplicationConfiguration()
    cfg.setTitle(game.configuration.title)
    cfg.setWindowedMode(game.configuration.width, game.configuration.height)
    cfg.useVsync(game.configuration.useVsync)
    cfg.setForegroundFPS(game.configuration.foregroundFPS)
    val waiter: CountDownLatch[IO] = CountDownLatch[IO](1).unsafeRunSync()
    val (createTopic, renderTopic, disposeTopic) =
      pipeline(waiter).unsafeRunSync()
    val app = new Lwjgl3Application(
      new ImpureGame(
        createTopic,
        renderTopic,
        disposeTopic
      )(waiter),
      cfg
    )

  type State
  def game: IOGame[State]
end IOAppGame

object Game extends IOAppGame:
  type State = (SpriteBatch, BitmapFont)
  def game: IOGame[State] = new IOGame[State]:
    val initialState: State = (null, null)
    def create(): IO[State] =
      val batch = new SpriteBatch()
      val font = new BitmapFont()
      IO.blocking(println("create")) *> IO((batch, font))

    def render(state: State): IO[Unit] =
      IO.blocking(println(s"render $state")) *> IO.blocking {
        val (batch, font) = state
        batch.begin()
        font.draw(
          batch,
          "Holi! uwu",
          Gdx.graphics.getWidth() / 2,
          Gdx.graphics.getHeight() / 2
        )
        batch.end()
      }
    def dispose(): IO[Unit] = IO.blocking(println("dispose")) *> IO.unit
    def update(state: State): IO[State] =
      IO.blocking(println("update")) *> IO(state)
end Game
