package com.ivmoreau.game
import cats.effect.IO

class Example extends IOGame[Int]:
  override def create(): IO[Int] = IO.pure(0)

  override def render(state: Int): IO[Unit] = IO.unit

  override def dispose(): IO[Unit] = IO.unit

  override def update(state: Int): IO[Int] = IO.unit.map(_ => state + 1)
end Example
