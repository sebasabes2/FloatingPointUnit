package floatingPointUnit

import chisel3._

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

