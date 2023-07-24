package com.ivmoreau.game

import cats.effect.{IO, Ref}
import com.ivmoreau.game.graphics.*
import com.ivmoreau.game.input.{Event, Key}

object Base extends IGameLogic:
  override type State = (Int, Double)

  override def init(): IO[(Int, Double)] = IO((0, 0.0d))

  override def onEvent(
      event: Event,
      stateRef: Ref[IO, (Int, Double)]
  ): IO[Unit] =
    event match
      case Event.KeyDown(a) if a == Key.A() =>
        stateRef.update { case (direction, color) => (1, color) }
      case Event.KeyUp(a) if a == Key.A() =>
        stateRef.update { case (direction, color) => (0, color) }
      case Event.KeyDown(a) if a == Key.D() =>
        stateRef.update { case (direction, color) => (-1, color) }
      case Event.KeyUp(a) if a == Key.D() =>
        stateRef.update { case (direction, color) => (0, color) }
      case other => IO(println(s"Unknown event: $other"))
  end onEvent

  override def update(stateRef: Ref[IO, (Int, Double)]): IO[Unit] =
    stateRef.get.flatMap { case (direction, color) =>
      val newColor = color + direction * 0.01d
      if newColor > 1.0d then stateRef.set((direction, 1.0d))
      else if newColor < 0.0d then stateRef.set((direction, 0.0d))
      else stateRef.set((direction, newColor))
    }

  override def render(stateRef: Ref[IO, (Int, Double)]) =
    stateRef.get.flatMap { case (direction, color) =>
      setClearColor(
        Color.Argb(color.toFloat, color.toFloat, color.toFloat, 1.0f)
      )
    } *> clear()

  override def dispose(): IO[Unit] = ???
end Base
