package FPU

import chisel3._

class ExponentMatcher extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint)
    val input2 = Input(new FloatingPoint)
    val larger = Output(new FloatingPoint)
    val smaller = Output(new FloatingPoint)
  })

  // Assume input2 has a smaller exponent than input1
  val exponentDifference = io.input1.exponent - io.input2.exponent
  io.larger := io.input1
  io.smaller.exponent := io.input1.exponent
  io.smaller.significand := io.input2.significand >> exponentDifference(6,0)
}
