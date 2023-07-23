package com.ivmoreau.game

import cats.effect.{IO, Ref}
import com.ivmoreau.game.input.Event

case class Configuration(
    title: String = "Game",
    width: Int = 800,
    height: Int = 480,
    resizable: Boolean = false
)

abstract class IOGame[State] extends RawIOGame[State]:
  val configuration: Configuration = Configuration(
    title = this.getClass.getSimpleName
  )

  def create(): IO[State]
  def render(state: State): IO[Unit]
  def onEvent(event: Event)(state: Ref[IO, State]): IO[Unit]
  def update(state: Ref[IO, State]): IO[Unit]
  def dispose(): IO[Unit]
end IOGame
