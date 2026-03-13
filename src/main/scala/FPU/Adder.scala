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
  val addition = larger.significand +& smaller.significand
  val subtraction = larger.significand - smaller.significand
  val effectiveOperation = larger.sign === smaller.sign
  io.output.significand := Mux(effectiveOperation, addition, subtraction)
}
