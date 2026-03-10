package FPU

import chisel3._

class ExponentMatcher extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint)
    val input2 = Input(new FloatingPoint)
    val larger = Output(new FloatingPoint)
    val smaller = Output(new FloatingPoint)
  })

  val input1Larger = io.input1.exponent >= io.input2.exponent
  val larger = Mux(input1Larger, io.input1, io.input2)
  val smaller = Mux(input1Larger, io.input2, io.input1)

  val exponentDifference = larger.exponent - smaller.exponent
  io.larger := larger
  io.smaller.sign := smaller.sign
  io.smaller.exponent := larger.exponent
  io.smaller.significand := smaller.significand >> exponentDifference(6,0)
}
