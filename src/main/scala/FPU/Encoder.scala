package FloatingPointUnit

import chisel3._

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
  io.output := Mux(io.input.nan, FloatingPoint.nan(exponentWidth, mantissaWidth), normal)
}
