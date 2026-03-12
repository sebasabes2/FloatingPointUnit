package FPU

import chisel3._

class Normalizer extends Module {
  val io = IO(new Bundle {
    val in = Input(new FloatingPoint)
    val out = Output(new FloatingPoint)
  })

  // TODO: make input width and output width parameterizable
  // TODO: give exception on overflow or underflow of exponent

  val normalizeRight = io.in.significand(24).asBool
  
  val leadingOneDetector = Module(new LeadingOneDetector(24))
  leadingOneDetector.io.in := io.in.significand(23,0)
  val normalizeLeftAmount = 23.U - leadingOneDetector.io.position

  io.out.sign := io.in.sign
  io.out.exponent := Mux(normalizeRight, io.in.exponent + 1.U, io.in.exponent - normalizeLeftAmount)
  io.out.significand := Mux(normalizeRight, io.in.significand(24,1), io.in.significand(23,0) << normalizeLeftAmount)
}
