package floatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
  })

  val subtract = io.larger.sign.asBool ^ io.smaller.sign.asBool
  val addition = io.larger.significandWithRoundBits() +& io.smaller.significandWithRoundBits()
  val subtraction = io.larger.significandWithRoundBits() - io.smaller.significandWithRoundBits()
  val result = Mux(subtract, 0.U(1.W) ## subtraction, addition)
  io.output.sign := io.larger.sign
  io.output.exponent := io.larger.exponent
  io.output.significand := result(significandWidth + 3,3)
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)

  // Special cases
  val oppositeInfinity = io.larger.infinity && io.smaller.infinity && (io.larger.sign.asBool ^ io.smaller.sign.asBool)
  io.output.infinity := io.larger.infinity || io.smaller.infinity
  io.output.denormal := false.B
  io.output.zero := false.B
  io.output.inexact := false.B
  io.output.nan := io.larger.nan || io.smaller.nan || oppositeInfinity
}
