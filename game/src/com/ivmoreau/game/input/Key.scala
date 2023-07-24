package com.ivmoreau.game.input

/** Represents a key on the keyboard. The key is identified by its code as
  * defined in the GLFW library.
  */
enum Key(val code: Int):
  case A() extends Key(65)
  case B() extends Key(66)
  case C() extends Key(67)
  case D() extends Key(68)
  case E() extends Key(69)
  case F() extends Key(70)
  case G() extends Key(71)
  case H() extends Key(72)
  case I() extends Key(73)
  case J() extends Key(74)
  case K() extends Key(75)
  case L() extends Key(76)
  case M() extends Key(77)
  case N() extends Key(78)
  case O() extends Key(79)
  case P() extends Key(80)
  case Q() extends Key(81)
  case R() extends Key(82)
  case S() extends Key(83)
  case T() extends Key(84)
  case U() extends Key(85)
  case V() extends Key(86)
  case W() extends Key(87)
  case X() extends Key(88)
  case Y() extends Key(89)
  case Z() extends Key(90)
  case AsKey(override val code: Int) extends Key(code)

  override def equals(obj: Any): Boolean = obj match
    case k: Key => k.code == code
    case _      => false
  end equals
end Key

object Key:
  /** Returns the key corresponding to the given code.
    *
    * @param n
    *   the code of the key as defined in the GLFW library
    * @return
    *   the key corresponding to the given code
    */
  def fromInt(n: Int): Option[Key] = n match
    case 65 => Some(A())
    case 66 => Some(B())
    case 67 => Some(C())
    case 68 => Some(D())
    case 69 => Some(E())
    case 70 => Some(F())
    case 71 => Some(G())
    case 72 => Some(H())
    case 73 => Some(I())
    case 74 => Some(J())
    case 75 => Some(K())
    case 76 => Some(L())
    case 77 => Some(M())
    case 78 => Some(N())
    case 79 => Some(O())
    case 80 => Some(P())
    case 81 => Some(Q())
    case 82 => Some(R())
    case 83 => Some(S())
    case 84 => Some(T())
    case 85 => Some(U())
    case 86 => Some(V())
    case 87 => Some(W())
    case 88 => Some(X())
    case 89 => Some(Y())
    case 90 => Some(Z())
    case _  => None
  end fromInt
end Key
