package com.ivmoreau.game.graphics

enum Color(val red: Float, val green: Float, val blue: Float, val alpha: Float):
  case Black() extends Color(0f, 0f, 0f, 1f)
  case White() extends Color(1f, 1f, 1f, 1f)
  case Red() extends Color(1f, 0f, 0f, 1f)
  case Green() extends Color(0f, 1f, 0f, 1f)
  case Blue() extends Color(0f, 0f, 1f, 1f)
  case LightGray() extends Color(0.75f, 0.75f, 0.75f, 1f)
  case Gray() extends Color(0.5f, 0.5f, 0.5f, 1f)
  case DarkGray() extends Color(0.25f, 0.25f, 0.25f, 1f)
  case Pink() extends Color(1f, 0.68f, 0.68f, 1f)
  case Orange() extends Color(1f, 0.78f, 0f, 1f)
  case Yellow() extends Color(1f, 1f, 0f, 1f)
  case Magenta() extends Color(1f, 0f, 1f, 1f)
  case Argb(
      override val red: Float,
      override val green: Float,
      override val blue: Float,
      override val alpha: Float
  ) extends Color(red, green, blue, alpha)
