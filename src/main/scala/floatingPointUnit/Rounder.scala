package floatingPointUnit

import chisel3._
import chisel3.util._
import math._

class Rounder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val roundingMode = Input(UInt(3.W))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
    val inexact = Output(Bool())
    val overflow = Output(Bool())
  })

  // Round to nearest, tie to even
  val nearestEven = io.input.guard & (io.input.significand(0) | io.input.round | io.input.sticky)

  // Round to nearest, tie away from zero
  val nearestAway = io.input.guard

  // Round towards positive infinity
  val positiveInf = !io.input.sign & (io.input.guard | io.input.round | io.input.sticky)

  // Round towards positive infinity
  val negativeInf = io.input.sign & (io.input.guard | io.input.round | io.input.sticky)

  val rnd = WireDefault(0.U(1.W))
  switch (io.roundingMode) {
    is (0.U) { rnd := nearestEven }
    is (1.U) { rnd := nearestAway }
    is (2.U) { rnd := positiveInf }
    is (3.U) { rnd := negativeInf }
  }

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
