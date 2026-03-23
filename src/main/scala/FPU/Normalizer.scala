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
  val normalizeLeft = !leadingOneDetector.io.zero
  val normalizeLeftAmount = (outputSignificandWidth - 1).U - leadingOneDetector.io.position

  val exponentNormalizedRight = io.input.exponent + 1.U
  val exponentNormalizedLeft = io.input.exponent - normalizeLeftAmount
  val exponentNormalized = Mux(normalizeRight, exponentNormalizedRight, Mux(normalizeLeft, exponentNormalizedLeft, io.input.exponent))

  val significandNormalizedRight = io.input.significandWithRoundBits()(outputSignificandWidth + 3, 1)
  val significandNormalizedLeft = io.input.significandWithRoundBits()(outputSignificandWidth + 2, 0) << normalizeLeftAmount
  val significandNormalized = Mux(normalizeRight, significandNormalizedRight, Mux(normalizeLeft, significandNormalizedLeft, io.input.significandWithRoundBits))
  
  io.output.sign := io.input.sign
  io.output.exponent := exponentNormalized
  io.output.significand := significandNormalized(outputSignificandWidth + 2,3)
  io.output.guard := significandNormalized(2)
  io.output.round := significandNormalized(1)
  io.output.sticky := significandNormalized(0)
}
