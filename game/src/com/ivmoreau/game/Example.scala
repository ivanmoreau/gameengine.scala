package com.ivmoreau.game
import cats.effect.{IO, Ref}
import cats.effect.std.{Console, Random}
import com.ivmoreau.game.input.Event
import concurrent.duration.DurationInt

// This example is a simple game that changes the background color
// when the user presses the A key. This happens after 1 second.
object Example extends IOGame[Int]:
  override def create(): IO[Int] =
    graphics.setClearColor(graphics.Color.Blue()) *>
      IO.pure(0)

  override def render(state: Int): IO[Unit] = {
    val f = graphics.Color.Blue()
    val t = graphics.Color.Red()
    if state == 1 then graphics.setClearColor(f)
    else graphics.setClearColor(t)
  } *>
    graphics.clear()

  override def dispose(): IO[Unit] = IO.unit

  override def update(state: Ref[IO, Int]): IO[Unit] =
    state.get.flatMap(s => Console[IO].println(s"Inside update!!"))

  override def onEvent(event: Event)(state: Ref[IO, Int]): IO[Unit] =
    println(s"Inside onEvent $event")
    event match
      case Event.KeyDown(key: input.Key.A) =>
        IO.sleep(1.seconds) *>
          Console[IO].println("UP A") *> state.set(1)
      case Event.KeyUp(key: input.Key.A) =>
        IO.sleep(1.seconds) *>
          Console[IO].println("Down A") *> state.set(0)
      case _ =>
        Console[IO].println("Other") *> IO.pure(state)
end Example
