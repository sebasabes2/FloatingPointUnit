package FPU

import chisel3._

class FloatingPoint extends Bundle {
  // TODO: Add sign
  val exponent = UInt(32.W) // TODO: make size parameterizable
  val significant = UInt(32.W) // TODO: make size parameterizable
}
