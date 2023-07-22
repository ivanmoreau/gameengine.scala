package com.ivmoreau.game
import cats.effect.IO

object Example extends IOGame[Int]:
  override def create(): IO[Int] =
    graphics.setClearColor(graphics.Color.Blue()) *>
      IO.pure(0)

  override def render(state: Int): IO[Unit] =
    graphics.clear()

  override def dispose(): IO[Unit] = IO.unit

  override def update(state: Int): IO[Int] = IO.unit.map(_ => state + 1)
end Example
