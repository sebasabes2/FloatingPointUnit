package floatingPointUnit

import chisel3._

class Rounder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth + 1))
    val inexact = Output(Bool())
  })

  // TODO: implement other rounding modes

  // Round to nearest, tie to even
  val rnd = io.input.guard & (io.input.significand(0) | io.input.round | io.input.sticky)
  io.output := io.input
  io.output.significand := io.input.significand +& rnd

  io.inexact := io.input.guard | io.input.round | io.input.sticky 
}
