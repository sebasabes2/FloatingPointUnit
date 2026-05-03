package floatingPointUnit

import chisel3._
import chisel3.util._
import math._

class RightNormalizer(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth + 1))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val normalize = io.input.significand(significandWidth).asBool
  val overflow = normalize && io.input.exponent >= (pow(2, exponentWidth).intValue - 2).U
  io.output := io.input
  io.output.exponent := io.input.exponent + normalize.asUInt
  io.output.significand := Mux(normalize, io.input.significand(significandWidth, 1), io.input.significand(significandWidth - 1, 0))
  io.output.guard := Mux(normalize, io.input.significand(0), io.input.guard)
  io.output.round := Mux(normalize, io.input.guard, io.input.round)
  io.output.sticky := Mux(normalize, io.input.round | io.input.sticky, io.input.sticky)
  io.output.infinity := io.input.infinity || overflow
}
