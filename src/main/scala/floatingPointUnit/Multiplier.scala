package floatingPointUnit

import chisel3._
import math._

class Multiplier(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val roundingMode = Input(UInt(3.W))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth * 2))
  })

  // Sign
  val sign = io.input1.sign ^ io.input2.sign
  io.output.sign := sign

  // Exponent
  val bias = pow(2, exponentWidth - 1).intValue - 1
  val exponent = io.input1.exponent +& io.input2.exponent
  val overflow = exponent >= (pow(2, exponentWidth).intValue - 1 + bias).U
  val notAllowOverflow = io.roundingMode === 1.U || io.roundingMode === 2.U && !sign || io.roundingMode === 3.U && sign.asBool
  val underflow = exponent <= bias.U
  val rightShiftAmount = Mux(underflow, (bias + 1).U - exponent, 0.U)
  io.output.exponent := Mux(overflow && notAllowOverflow, (pow(2, exponentWidth).intValue - 2).U, Mux(underflow, 1.U, exponent - bias.U))

  // Significand
  val product = io.input1.significand * io.input2.significand
  val productShifted = (product ## 0.U(2.W)) >> rightShiftAmount
  io.output.significand := Mux(overflow && notAllowOverflow, Seq.fill(significandWidth * 2)(1.U(1.W)).reduce((x, y) => x ## y), productShifted(significandWidth * 2 + 1, 2))

  // Round bits
  io.output.guard := Mux(overflow && notAllowOverflow, 0.U, productShifted(1))
  io.output.round := Mux(overflow && notAllowOverflow, 0.U, productShifted(0))
  val stickyBitDetector = Module(new StickyBitDetector(significandWidth * 2 + 2))
  stickyBitDetector.io.input := product ## 0.U(2.W)
  stickyBitDetector.io.shift := rightShiftAmount
  io.output.sticky := Mux(overflow && notAllowOverflow, 0.U, stickyBitDetector.io.sticky || rightShiftAmount >= (significandWidth * 2 + 2).U)

  // Special cases
  val nan = (io.input1.significand === 0.U && io.input2.infinity) || (io.input2.significand === 0.U && io.input1.infinity)
  io.output.infinity := io.input1.infinity || io.input2.infinity || (overflow && !notAllowOverflow)
  io.output.denormal := underflow
  io.output.zero := false.B
  io.output.inexact := false.B
  io.output.nan := nan || io.input1.nan || io.input2.nan
}
