package floatingPointUnit

import chisel3._
import chisel3.util._
import math._

class RightNormalizer(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth + 1))
    val roundingMode = Input(UInt(3.W))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val normalize = io.input.significand(significandWidth).asBool
  val overflow = normalize && io.input.exponent >= (pow(2, exponentWidth).intValue - 2).U
  val notAllowOverflow = io.roundingMode === 1.U || io.roundingMode === 2.U && !io.input.sign || io.roundingMode === 3.U && io.input.sign.asBool
  io.output := io.input
  io.output.exponent := Mux(overflow && notAllowOverflow, (pow(2, exponentWidth).intValue - 2).U, io.input.exponent + normalize.asUInt)
  io.output.significand := Mux(overflow && notAllowOverflow, Seq.fill(significandWidth)(1.U(1.W)).reduce((x, y) => x ## y), Mux(normalize, io.input.significand(significandWidth, 1), io.input.significand(significandWidth - 1, 0)))
  io.output.guard := Mux(overflow && notAllowOverflow, 0.U, Mux(normalize, io.input.significand(0), io.input.guard))
  io.output.round := Mux(overflow && notAllowOverflow, 0.U, Mux(normalize, io.input.guard, io.input.round))
  io.output.sticky := Mux(overflow && notAllowOverflow, 0.U, Mux(normalize, io.input.round | io.input.sticky, io.input.sticky))
  io.output.infinity := io.input.infinity || (overflow && !notAllowOverflow)
}
