package FloatingPointUnit

import chisel3._
import math._

class Encoder(exponentWidth: Int, mantissaWidth: Int) extends Module {
  val floatingPointWidth = 1 + exponentWidth + mantissaWidth
  val significandWidth = mantissaWidth + 1

  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(UInt(floatingPointWidth.W))
  })

  val denormal = !io.input.significand(significandWidth - 1)
  val exponent = Mux(denormal, 0.U, io.input.exponent)
  val normal = io.input.sign ## Mux(denormal, 0.U, exponent) ## io.input.significand(significandWidth - 2,0) // TODO: subtract bias
  val nan = 0.U(1.W) ## (pow(2, exponentWidth).intValue - 1).U(exponentWidth.W) ## 1.U(1.W) ## 0.U((mantissaWidth - 1).W)
  val infinity = io.input.sign ## (pow(2, exponentWidth).intValue - 1).U(exponentWidth.W) ## 0.U(mantissaWidth.W)
  io.output := Mux(io.input.nan, nan, Mux(io.input.infinity, infinity, normal))
}
