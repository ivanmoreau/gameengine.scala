package com.ivmoreau.game.input

import org.lwjgl.glfw.GLFW

enum Event:
  case KeyDown(key: Key)
  case KeyUp(key: Key)
end Event

object Event:
  def fromKeyAction(key: Int, action: Int): Option[Event] =
    if action == GLFW.GLFW_PRESS then Key.fromInt(key).map(Event.KeyDown(_))
    else if action == GLFW.GLFW_RELEASE then
      Key.fromInt(key).map(Event.KeyUp(_))
    else None
  end fromKeyAction
end Event
