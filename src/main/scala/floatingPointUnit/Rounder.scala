package floatingPointUnit

import chisel3._
import math._

class Rounder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
    val inexact = Output(Bool())
    val overflow = Output(Bool())
  })

  // TODO: implement other rounding modes

  // Round to nearest, tie to even
  val rnd = io.input.guard & (io.input.significand(0) | io.input.round | io.input.sticky)

  // Normalize result
  val result = io.input.significand +& rnd
  val normalize = result(significandWidth).asBool
  val overflow = normalize && io.input.exponent >= (pow(2, exponentWidth).intValue - 2).U
  io.output := io.input
  io.output.exponent := io.input.exponent + normalize.asUInt
  io.output.significand := Mux(normalize, result(significandWidth, 1), result(significandWidth - 1, 0))
  io.output.infinity := overflow || io.input.infinity

  io.inexact := io.input.guard | io.input.round | io.input.sticky
  io.overflow := overflow
}
