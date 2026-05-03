package floatingPointUnit

import chisel3._

class Shortener(exponentWidth: Int, inputSignificandWidth: Int, outputSignificandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, inputSignificandWidth))
    val output = Output(new FloatingPoint(exponentWidth, outputSignificandWidth))
  })

  io.output := io.input
  io.output.significand := io.input.significand(inputSignificandWidth - 1, inputSignificandWidth - outputSignificandWidth)
  io.output.guard := io.input.significand(inputSignificandWidth - outputSignificandWidth - 1)
  io.output.round := io.input.significand(inputSignificandWidth - outputSignificandWidth - 2)
  io.output.sticky := io.input.significand(inputSignificandWidth - outputSignificandWidth - 3, 0).orR | io.input.guard | io.input.round
}
