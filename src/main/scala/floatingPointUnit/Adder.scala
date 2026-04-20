package floatingPointUnit

import chisel3._

class Adder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth + 3))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth + 3))
    val subtract = Input(Bool())
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
    val nan = Output(Bool())
  })

  val firstStep = RegNext(io.input1.significand(7,0) +& io.input2.significand(7,0) +& io.subtract.asUInt)
  val secondStep = RegNext(io.input1.significand(significandWidth + 2, 8)) +& RegNext(io.input2.significand(significandWidth + 2, 8)) +& firstStep(8)
  val result = secondStep ## firstStep(7,0)
  io.output := RegNext(io.input1)
  io.output.significand := Mux(RegNext(io.subtract), result(significandWidth + 2,3), result(significandWidth + 3,3))
  io.output.guard := result(2)
  io.output.round := result(1)
  io.output.sticky := result(0)

  // Special cases
  val nanInput = io.input1.nan || io.input2.nan
  val oppositeInfinity = io.input1.infinity && io.input2.infinity && (io.input1.sign.asBool ^ io.input2.sign.asBool)
  val nan = nanInput || oppositeInfinity
  io.output.nan := nan

  io.nan := nan
}
