package FloatingPointUnit

import chisel3._
import math._

class Decoder(exponentWidth: Int, mantissaWidth: Int) extends Module {
  val floatingPointWidth = 1 + exponentWidth + mantissaWidth
  val significandWidth = mantissaWidth + 1

  val io = IO(new Bundle {
    val input = Input(UInt(floatingPointWidth.W))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val sign = io.input(floatingPointWidth - 1)
  val exponent = io.input(mantissaWidth + exponentWidth - 1, mantissaWidth) // TODO: add bias
  val mantissa = io.input(mantissaWidth - 1,0)

  val subnormal = exponent === 0.U
  val supernormal = exponent === (pow(2, exponentWidth).intValue - 1).U
  val infinity = supernormal && mantissa === 0.U
  val nan = supernormal && mantissa =/= 0.U

  io.output.sign := sign
  io.output.exponent := Mux(subnormal, 1.U, exponent)
  io.output.significand := !subnormal ## mantissa
  io.output.guard := 0.U
  io.output.round := 0.U
  io.output.sticky := 0.U
  io.output.infinity := infinity
  io.output.nan := nan
}
