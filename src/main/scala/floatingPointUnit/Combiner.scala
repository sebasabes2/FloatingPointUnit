package floatingPointUnit

import chisel3._
import chisel3.util._

class Combiner(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val addition = Input(new FloatingPoint(exponentWidth, significandWidth))
    val multiplication = Input(new FloatingPoint(exponentWidth, significandWidth))
    val operation = Input(UInt(2.W))
    val combined = Output(new FloatingPoint(exponentWidth, significandWidth))
  })

  io.combined.sign := 0.U
  io.combined.exponent := 0.U
  io.combined.significand := 0.U
  io.combined.guard := 0.U
  io.combined.round := 0.U
  io.combined.sticky := 0.U
  io.combined.nan := false.B
  io.combined.infinity := false.B

  switch (io.operation) {
    is (0.U) { io.combined := io.addition }
    is (1.U) { io.combined := io.addition }
    is (2.U) { io.combined := io.multiplication }
  }
}
