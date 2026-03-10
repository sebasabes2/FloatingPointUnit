package FPU

import chisel3._

class SignificandAdder extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint)
    val smaller = Input(new FloatingPoint)
    val out = Output(new FloatingPoint)
  })

  io.out.exponent := io.larger.exponent
  io.out.significand := io.smaller.significand +& io.larger.significand
}
