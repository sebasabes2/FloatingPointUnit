package FPU

import chisel3._

class FloatingPoint extends Bundle {
  // TODO: Add sign
  val exponent = UInt(32.W) // TODO: make size parameterizable
  val significand = UInt(32.W) // TODO: make size parameterizable

  def encode(): UInt = 0.U(1.W) ## exponent(7,0) ## significand(22,0)
}
