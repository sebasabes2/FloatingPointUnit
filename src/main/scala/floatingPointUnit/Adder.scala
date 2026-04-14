package floatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
    val nan = Output(Bool())
  })

  val addition = io.larger.significandWithRoundBits() +& io.smaller.significandWithRoundBits()
  val subtraction = io.larger.significandWithRoundBits() - io.smaller.significandWithRoundBits()
  val effectiveOperation = io.larger.sign === io.smaller.sign
  val result = Mux(effectiveOperation, addition, subtraction)
  io.output := io.larger
  io.output.significand := result(significandWidth + 3,3)
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)

  // Special cases
  val nanInput = io.larger.nan || io.smaller.nan
  val oppositeInfinity = io.larger.infinity && io.smaller.infinity && (io.larger.sign.asBool ^ io.smaller.sign.asBool)
  val nan = nanInput || oppositeInfinity
  io.output.nan := nan

  io.nan := nan
}
