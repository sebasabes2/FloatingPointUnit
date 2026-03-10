package FPU

import chisel3._

class FloatingPoint extends Bundle {
  // TODO: Add sign
  val exponent = UInt(32.W) // TODO: make size parameterizable
  val significant = UInt(32.W) // TODO: make size parameterizable

  def encode(): UInt = 0.U(1.W) ## exponent(7,0) ## significant(22,0)
}
