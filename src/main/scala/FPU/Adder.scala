package FloatingPointUnit

import chisel3._

class Adder extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint)
    val input2 = Input(new FloatingPoint)
    val output = Output(new FloatingPoint)
  })

  val exponentMatcher = Module(new ExponentMatcher)
  exponentMatcher.io.input1 := io.input1
  exponentMatcher.io.input2 := io.input2

  val larger = exponentMatcher.io.larger
  val smaller = exponentMatcher.io.smaller

  io.output.sign := larger.sign
  io.output.exponent := larger.exponent
  val largerSignificand = larger.significand ## larger.guard ## larger.round ## larger.sticky
  val smallerSignificand = smaller.significand ## smaller.guard ## smaller.round ## smaller.sticky
  val addition = largerSignificand +& smallerSignificand
  val subtraction = largerSignificand - smallerSignificand
  val effectiveOperation = larger.sign === smaller.sign
  val result = Mux(effectiveOperation, addition, subtraction)
  io.output.significand := result(34,3)
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)
}
