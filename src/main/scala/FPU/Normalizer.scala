package FPU

import chisel3._

class Normalizer extends Module {
  val io = IO(new Bundle {
    val in = Input(new FloatingPoint)
    val out = Output(new FloatingPoint)
  })

  // TODO: make it normalize in both directions
  // TODO: make input width and output width parameterizable

  val should_normalize = io.in.significand(24).asBool
  io.out.exponent := Mux(should_normalize, io.in.exponent + 1.U, io.in.exponent)
  io.out.significand := Mux(should_normalize, io.in.significand(24,1), io.in.significand(23,0))
}
