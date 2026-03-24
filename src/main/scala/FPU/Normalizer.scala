package FloatingPointUnit

import chisel3._
import math._

class Normalizer(exponentWidth: Int, outputSignificandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, outputSignificandWidth + 1))
    val output = Output(new FloatingPoint(exponentWidth, outputSignificandWidth))
    val overflow = Output(Bool())
    val underflow = Output(Bool())
  })

  // TODO: consider making input width parameterizable

  val normalizeRight = io.input.significand(outputSignificandWidth).asBool
  val overflow = normalizeRight && io.input.exponent >= (pow(2, exponentWidth).intValue - 2).U

  val leadingOneDetector = Module(new LeadingOneDetector(outputSignificandWidth))
  leadingOneDetector.io.input := io.input.significand(outputSignificandWidth - 1,0)
  val normalizeLeft = !leadingOneDetector.io.zero
  val idealNormalizeLeftAmount = (outputSignificandWidth - 1).U - leadingOneDetector.io.position
  val underflow = idealNormalizeLeftAmount >= io.input.exponent
  val normalizeLeftAmount = Mux(underflow, io.input.exponent - 1.U, idealNormalizeLeftAmount)

  val exponentNormalizedRight = io.input.exponent + 1.U
  val exponentNormalizedLeft = io.input.exponent - normalizeLeftAmount
  val exponentNormalized = Mux(normalizeRight, exponentNormalizedRight, Mux(normalizeLeft, exponentNormalizedLeft, io.input.exponent))

  val significandNormalizedRight = io.input.significandWithRoundBits()(outputSignificandWidth + 3, 1)
  val significandNormalizedLeft = io.input.significandWithRoundBits()(outputSignificandWidth + 2, 0) << normalizeLeftAmount
  val significandNormalized = Mux(normalizeRight, significandNormalizedRight, Mux(normalizeLeft, significandNormalizedLeft, io.input.significandWithRoundBits))

  io.output := io.input
  io.output.exponent := exponentNormalized
  io.output.significand := significandNormalized(outputSignificandWidth + 2,3)
  io.output.guard := significandNormalized(2)
  io.output.round := significandNormalized(1)
  io.output.sticky := significandNormalized(0)
  io.output.infinity := overflow || io.input.infinity

  io.overflow := overflow
  io.underflow := underflow
}
