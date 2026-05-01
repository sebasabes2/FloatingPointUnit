package floatingPointUnit

import chisel3._

class ExponentMatcher(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val subtraction = Input(Bool())
    val larger = Output(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val input1 = io.input1
  val input2 = Wire(new FloatingPoint(exponentWidth, significandWidth))
  input2 := io.input2
  input2.sign := io.input2.sign ^ io.subtraction

  val sameExponent = input1.exponent === input2.exponent
  val exponent1Larger = input1.exponent > input2.exponent
  val sameSignificand = input1.significand === input2.significand
  val significand1Larger = input1.significand > input2.significand
  val input1Positive = !input1.sign
  val input1Larger = Mux(sameExponent, Mux(sameSignificand, input1Positive, significand1Larger), exponent1Larger)

  val larger = Mux(input1Larger, input1, input2)
  val smaller = Mux(input1Larger, input2, input1)

  val exponentDifference = larger.exponent - smaller.exponent
  io.larger := larger
  io.smaller := smaller
  io.smaller.exponent := larger.exponent
  val shiftedSignificand = (smaller.significand ## 0.U(2.W)) >> exponentDifference
  io.smaller.significand := shiftedSignificand(significandWidth + 1, 2)
  io.smaller.guard := shiftedSignificand(1)
  io.smaller.round := shiftedSignificand(0)

  val stickyBitDetector = Module(new StickyBitDetector(significandWidth + 2))
  stickyBitDetector.io.input := smaller.significand ## 0.U(2.W)
  stickyBitDetector.io.shift := exponentDifference
  io.smaller.sticky := stickyBitDetector.io.sticky || exponentDifference >= (significandWidth + 2).U
}
