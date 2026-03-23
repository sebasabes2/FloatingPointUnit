package FloatingPointUnit

import chisel3._

class ExponentMatcher(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val larger = Output(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val sameExponent = io.input1.exponent === io.input2.exponent
  val exponent1Larger = io.input1.exponent > io.input2.exponent
  val sameSignificand = io.input1.significand === io.input2.significand
  val significand1Larger = io.input1.significand > io.input2.significand
  val input1Positive = !io.input1.sign
  val input1Larger = Mux(sameExponent, Mux(sameSignificand, input1Positive, significand1Larger), exponent1Larger)

  val larger = Mux(input1Larger, io.input1, io.input2)
  val smaller = Mux(input1Larger, io.input2, io.input1)

  val exponentDifference = larger.exponent - smaller.exponent
  io.larger := larger
  io.smaller := smaller
  io.smaller.exponent := larger.exponent
  val shiftedSignificand = (smaller.significand ## 0.U(significandWidth.W)) >> exponentDifference
  io.smaller.significand := shiftedSignificand(2 * significandWidth - 1, significandWidth)
  io.smaller.guard := shiftedSignificand(significandWidth - 1)
  io.smaller.round := shiftedSignificand(significandWidth - 2)
  io.smaller.sticky := shiftedSignificand(significandWidth - 3, 0).orR
}
