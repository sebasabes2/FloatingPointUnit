package FloatingPointUnit

import chisel3._

class Normalizer(exponentWidth: Int, outputSignificandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, outputSignificandWidth + 1))
    val output = Output(new FloatingPoint(exponentWidth, outputSignificandWidth))
  })

  // TODO: consider making input width parameterizable
  // TODO: give exception on overflow or underflow of exponent

  val normalizeRight = io.input.significand(outputSignificandWidth).asBool
  
  val leadingOneDetector = Module(new LeadingOneDetector(outputSignificandWidth))
  leadingOneDetector.io.input := io.input.significand(outputSignificandWidth - 1,0)
  val normalizeLeftAmount = (outputSignificandWidth - 1).U - leadingOneDetector.io.position

  io.output.sign := io.input.sign
  io.output.exponent := Mux(normalizeRight, io.input.exponent + 1.U, io.input.exponent - normalizeLeftAmount)

  val normalizedRightValue = io.input.significandWithRoundBits()(outputSignificandWidth + 3, 1)
  val normalizedLeftValue = io.input.significandWithRoundBits()(outputSignificandWidth + 2, 0) << normalizeLeftAmount
  val normalizedValue = Mux(normalizeRight, normalizedRightValue, normalizedLeftValue)
  io.output.significand := normalizedValue(outputSignificandWidth + 2,3)
  io.output.guard := normalizedValue(2)
  io.output.round := normalizedValue(1)
  io.output.sticky := normalizedValue(0)
}
