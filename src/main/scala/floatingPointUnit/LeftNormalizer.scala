package floatingPointUnit

import chisel3._
import math._

class LeftNormalizer(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(new FloatingPoint(exponentWidth, significandWidth))
    val output = Output(new FloatingPoint(exponentWidth, significandWidth))
    val underflow = Output(Bool())
    val zero = Output(Bool())
  })

  val leadingOneDetector = Module(new LeadingOneDetector(significandWidth + 3))
  leadingOneDetector.io.input := io.input.significandWithRoundBits()(significandWidth + 2, 0)
  val underflow = leadingOneDetector.io.position >= io.input.exponent
  val normalizeLeftAmount = Mux(underflow, io.input.exponent - 1.U, leadingOneDetector.io.position)

  io.output := io.input
  io.output.exponent := io.input.exponent - normalizeLeftAmount
  val significand = io.input.significandWithRoundBits()(significandWidth + 2, 0) << normalizeLeftAmount
  io.output.significand := significand(significandWidth + 2,3)
  io.output.guard := significand(2)
  io.output.round := significand(1)
  io.underflow := underflow
  io.zero := leadingOneDetector.io.zero
}
