package floatingPointUnit

import chisel3._
import math._

class Encoder(exponentWidth: Int, fractionWidth: Int) extends Module {
  val floatingPointWidth = 1 + exponentWidth + fractionWidth
  val significandWidth = fractionWidth + 1

  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(UInt(floatingPointWidth.W))
    val flags = Output(new Bundle {
      val overflow = Bool()
      val underflow = Bool()
      val zero = Bool()
      val inexact = Bool()
      val nan = Bool()
    })
  })

  val default = io.input.sign ## Mux(io.input.denormal, 0.U, io.input.exponent) ## io.input.significand(significandWidth - 2,0)
  val nan = 0.U(1.W) ## (pow(2, exponentWidth).intValue - 1).U(exponentWidth.W) ## 1.U(1.W) ## 0.U((fractionWidth - 1).W)
  val infinity = io.input.sign ## (pow(2, exponentWidth).intValue - 1).U(exponentWidth.W) ## 0.U(fractionWidth.W)
  io.output := Mux(io.input.nan, nan, Mux(io.input.infinity, infinity, default))

  // Flags
  io.flags.overflow := io.input.infinity
  io.flags.underflow := io.input.denormal
  io.flags.zero := io.input.zero
  io.flags.inexact := io.input.inexact
  io.flags.nan := io.input.nan
}
