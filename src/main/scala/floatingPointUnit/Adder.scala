package floatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth + 3))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth + 3))
    val subtract = Input(Bool())
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
  })

  val result = io.input1.significand +& io.input2.significand +& io.subtract.asUInt
  io.output.sign := io.input1.sign
  io.output.exponent := io.input1.exponent
  io.output.significand := Mux(io.subtract, result(significandWidth + 2,3), result(significandWidth + 3,3))
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)

  // Special cases
  val oppositeInfinity = io.input1.infinity && io.input2.infinity && (io.input1.sign.asBool ^ io.input2.sign.asBool)
  io.output.infinity := io.input1.infinity || io.input2.infinity
  io.output.denormal := false.B
  io.output.zero := false.B
  io.output.inexact := false.B
  io.output.nan := io.input1.nan || io.input2.nan || oppositeInfinity
}
