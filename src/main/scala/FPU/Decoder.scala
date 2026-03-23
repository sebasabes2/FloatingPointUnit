package FloatingPointUnit

import chisel3._

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

  io.output.sign := sign
  io.output.exponent := exponent
  io.output.significand := 1.U(1.W) ## mantissa
  io.output.guard := 0.U
  io.output.round := 0.U
  io.output.sticky := 0.U
}
