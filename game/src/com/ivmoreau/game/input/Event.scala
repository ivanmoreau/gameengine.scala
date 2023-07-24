package com.ivmoreau.game.input

import org.lwjgl.glfw.GLFW

enum Event:
  case KeyDown(key: Key)
  case KeyUp(key: Key)
end Event

object Event:
  def fromKeyAction(key: Int, action: Int): Option[Event] =
    if action == GLFW.GLFW_PRESS then Some(Event.KeyDown(Key.AsKey(key)))
    else if action == GLFW.GLFW_RELEASE then Some(Event.KeyUp(Key.AsKey(key)))
    else None
  end fromKeyAction
end Event
