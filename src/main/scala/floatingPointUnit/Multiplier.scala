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
  // TODO: expirent if overflow method has an impact
  // val overflow = exponent(exponentWidth) || (exponent(exponentWidth - 1, 0) === (pow(2, exponentWidth).intValue - 1))
  val overflow = exponent >= (pow(2, exponentWidth).intValue - 1 + bias).U
  val underflow = exponent <= bias.U
  io.output.exponent := exponent

  // Significand
  io.output.significand := io.input1.significand * io.input2.significand

  // Round bits
  io.output.guard := 0.U
  io.output.round := 0.U
  io.output.sticky := 0.U

  // Special cases
  val zero = (io.input1.significand === 0.U && io.input2.infinity) && (io.input2.significand === 0.U && io.input1.infinity)
  io.output.nan := zero || io.input1.nan || io.input2.nan
  io.output.infinity := io.input1.infinity || io.input2.infinity || overflow
  io.overflow := overflow
  io.underflow := underflow
  io.zero := zero
  io.nan := zero
}
