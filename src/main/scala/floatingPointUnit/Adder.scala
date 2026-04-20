package floatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
    val nan = Output(Bool())
  })

  val subtract = io.larger.sign.asBool ^ io.smaller.sign.asBool
  val larger = io.larger.significandWithRoundBits()
  val smaller = Mux(subtract, 1.U(1.W) ## ~io.smaller.significandWithRoundBits(), io.smaller.significandWithRoundBits())
  val result = larger + smaller + subtract.asUInt()
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
