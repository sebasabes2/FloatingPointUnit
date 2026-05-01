package floatingPointUnit

import chisel3._
import math._

class Normalizer(exponentWidth: Int, outputSignificandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, outputSignificandWidth + 1))
    val output = Output(new FloatingPoint(exponentWidth, outputSignificandWidth))
    val overflow = Output(Bool())
    val underflow = Output(Bool())
    val zero = Output(Bool())
  })

  // TODO: consider making input width parameterizable

  val normalizeRight = io.input.significand(outputSignificandWidth).asBool
  val overflow = normalizeRight && io.input.exponent >= (pow(2, exponentWidth).intValue - 2).U

  val leadingOneDetector = Module(new LeadingOneDetector(outputSignificandWidth + 3))
  leadingOneDetector.io.input := io.input.significandWithRoundBits()(outputSignificandWidth + 2, 0)
  val underflow = leadingOneDetector.io.position >= io.input.exponent
  val normalizeLeftAmount = Mux(underflow, io.input.exponent - 1.U, leadingOneDetector.io.position)

  val exponentNormalizedRight = io.input.exponent + 1.U
  val exponentNormalizedLeft = io.input.exponent - normalizeLeftAmount
  val exponentNormalized = Mux(normalizeRight, exponentNormalizedRight, exponentNormalizedLeft)

  val significandNormalizedRight = io.input.significandWithRoundBits()(outputSignificandWidth + 3, 1)
  val significandNormalizedLeft = io.input.significandWithRoundBits()(outputSignificandWidth + 2, 0) << normalizeLeftAmount
  val significandNormalized = Mux(normalizeRight, significandNormalizedRight, significandNormalizedLeft)

  io.output := io.input
  io.output.exponent := exponentNormalized
  io.output.significand := significandNormalized(outputSignificandWidth + 2,3)
  io.output.guard := significandNormalized(2)
  io.output.round := significandNormalized(1)
  io.output.sticky := significandNormalized(0) || io.input.sticky.asBool
  io.output.infinity := overflow || io.input.infinity

  io.overflow := overflow
  io.underflow := underflow
  io.zero := !normalizeRight && leadingOneDetector.io.zero
}
