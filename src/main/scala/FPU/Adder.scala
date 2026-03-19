package FloatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
  })

  val exponentMatcher = Module(new ExponentMatcher(exponentWidth, significandWidth))
  exponentMatcher.io.input1 := io.input1
  exponentMatcher.io.input2 := io.input2

  val larger = exponentMatcher.io.larger
  val smaller = exponentMatcher.io.smaller

  io.output.sign := larger.sign
  io.output.exponent := larger.exponent
  val addition = larger.significandWithRoundBits() +& smaller.significandWithRoundBits()
  val subtraction = larger.significandWithRoundBits() - smaller.significandWithRoundBits()
  val effectiveOperation = larger.sign === smaller.sign
  val result = Mux(effectiveOperation, addition, subtraction)
  io.output.significand := result(significandWidth + 3,3)
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)
}
