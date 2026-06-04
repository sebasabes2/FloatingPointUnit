package floatingPointUnit

import chisel3._
import math._

class Decoder(exponentWidth: Int, fractionWidth: Int) extends Module {
  val floatingPointWidth = 1 + exponentWidth + fractionWidth
  val significandWidth = fractionWidth + 1

  val io = IO(new Bundle {
    val input = Input(UInt(floatingPointWidth.W))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  val sign = io.input(floatingPointWidth - 1)
  val exponent = io.input(fractionWidth + exponentWidth - 1, fractionWidth)
  val fraction = io.input(fractionWidth - 1,0)

  val denormal = exponent === 0.U
  val supernormal = exponent === (pow(2, exponentWidth).intValue - 1).U
  val infinity = supernormal && fraction === 0.U
  val nan = supernormal && fraction =/= 0.U

  io.output.sign := sign
  io.output.exponent := Mux(denormal, 1.U, exponent)
  io.output.significand := !denormal ## fraction
  io.output.guard := 0.U
  io.output.round := 0.U
  io.output.sticky := 0.U
  io.output.infinity := infinity
  io.output.denormal := denormal
  io.output.zero := false.B
  io.output.inexact := false.B
  io.output.nan := nan
}
