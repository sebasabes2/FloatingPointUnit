package FPU

import chisel3._

class SignificandAdder extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint)
    val smaller = Input(new FloatingPoint)
    val out = Output(new FloatingPoint)
  })

  io.out.sign := io.larger.sign
  io.out.exponent := io.larger.exponent
  val addition = io.larger.significand +& io.smaller.significand
  val subtraction = io.larger.significand - io.smaller.significand
  io.out.significand := Mux(io.larger.sign === io.smaller.sign, addition, subtraction)
}
