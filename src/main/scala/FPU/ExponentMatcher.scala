package FloatingPointUnit

import chisel3._

class ExponentMatcher extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint)
    val input2 = Input(new FloatingPoint)
    val larger = Output(new FloatingPoint)
    val smaller = Output(new FloatingPoint)
  })

  val sameExponent = io.input1.exponent === io.input2.exponent
  val input1Larger = Mux(sameExponent, io.input1.significand > io.input2.significand, io.input1.exponent > io.input2.exponent)

  val larger = Mux(input1Larger, io.input1, io.input2)
  val smaller = Mux(input1Larger, io.input2, io.input1)

  val exponentDifference = larger.exponent - smaller.exponent
  io.larger := larger
  io.smaller.sign := smaller.sign
  io.smaller.exponent := larger.exponent
  val shiftedSignificand = (smaller.significand ## 0.U(32.W)) >> exponentDifference(6,0)
  io.smaller.significand := shiftedSignificand(63, 32)
  io.smaller.guard := shiftedSignificand(31)
  io.smaller.round := shiftedSignificand(30)
  io.smaller.sticky := shiftedSignificand(29, 0).orR
}
