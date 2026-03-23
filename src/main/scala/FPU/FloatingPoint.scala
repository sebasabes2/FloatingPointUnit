package FloatingPointUnit

import chisel3._
import math._

class FloatingPoint(exponentWidth: Int, significandWidth: Int) extends Bundle {
  val sign = UInt(1.W)
  val exponent = UInt(exponentWidth.W)
  val significand = UInt(significandWidth.W)

  val guard = UInt(1.W)
  val round = UInt(1.W)
  val sticky = UInt(1.W)

  val infinity = Bool()
  val nan = Bool()

  def significandWithRoundBits(): UInt = significand ## guard ## round ## sticky
}

object FloatingPoint {
  def nan(exponentWidth: Int, mantissaWidth: Int): UInt = 0.U(1.W) ## (pow(2, exponentWidth).intValue - 1).U(exponentWidth.W) ## 1.U(1.W) ## 0.U((mantissaWidth - 1).W)
}
