package floatingPointUnit

import chisel3._
import math._

class Multiplier(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input1 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input2 = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth * 2))
    val overflow = Output(Bool())
    val underflow = Output(Bool())
    val zero = Output(Bool())
    val nan = Output(Bool())
  })

  // Sign
  io.output.sign := io.input1.sign ^ io.input2.sign

  // Exponent
  val bias = pow(2, exponentWidth - 1).intValue - 1
  val exponent = io.input1.exponent +& io.input2.exponent
  val overflow = exponent >= (pow(2, exponentWidth).intValue - 1 + bias).U
  val underflow = exponent <= bias.U
  val rightShiftAmount = Mux(underflow, (bias + 1).U - exponent, 0.U)
  io.output.exponent := Mux(underflow, 1.U, exponent - bias.U)

  // Significand
  val product = io.input1.significand * io.input2.significand
  val productShifted = (product ## 0.U(2.W)) >> rightShiftAmount
  io.output.significand := productShifted(significandWidth * 2 + 1, 2)

  // Round bits
  io.output.guard := productShifted(1)
  io.output.round := productShifted(0)
  val stickyBitDetector = Module(new StickyBitDetector(significandWidth * 2 + 2))
  stickyBitDetector.io.input := product ## 0.U(2.W)
  stickyBitDetector.io.shift := rightShiftAmount
  io.output.sticky := stickyBitDetector.io.sticky || rightShiftAmount >= (significandWidth * 2 + 2).U

  // Special cases
  val zero = (io.input1.significand === 0.U && io.input2.infinity) || (io.input2.significand === 0.U && io.input1.infinity)
  io.output.nan := zero || io.input1.nan || io.input2.nan
  io.output.infinity := io.input1.infinity || io.input2.infinity || overflow
  io.overflow := overflow
  io.underflow := underflow
  io.zero := zero
  io.nan := zero
}
