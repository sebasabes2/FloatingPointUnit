package FloatingPointUnit

import chisel3._

class Normalizer extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint)
    val output = Output(new FloatingPoint)
  })

  // TODO: make input width and output width parameterizable
  // TODO: give exception on overflow or underflow of exponent

  val normalizeRight = io.input.significand(24).asBool
  
  val leadingOneDetector = Module(new LeadingOneDetector(24))
  leadingOneDetector.io.input := io.input.significand(23,0)
  val normalizeLeftAmount = 23.U - leadingOneDetector.io.position

  io.output.sign := io.input.sign
  io.output.exponent := Mux(normalizeRight, io.input.exponent + 1.U, io.input.exponent - normalizeLeftAmount)

  val value = io.input.significand ## io.input.guard ## io.input.round ## io.input.sticky
  val normalizedValue = Mux(normalizeRight, value(27,1), value(26,0) << normalizeLeftAmount)
  io.output.significand := normalizedValue(26,3)
  io.output.guard := normalizedValue(2)
  io.output.round := normalizedValue(1)
  io.output.sticky := normalizedValue(0)
}
