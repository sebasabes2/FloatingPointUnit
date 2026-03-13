package FloatingPointUnit

import chisel3._

class Rounder extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint)
    val output = Output(new FloatingPoint)
  })

  // TODO: implement other rounding modes

  // Round to nearest, tie to even
  val rnd = io.input.guard & (io.input.significand(0) | io.input.round | io.input.sticky)
  io.output := io.input
  io.output.significand := io.input.significand + rnd
}
