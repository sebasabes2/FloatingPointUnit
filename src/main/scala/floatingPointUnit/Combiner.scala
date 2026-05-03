package floatingPointUnit

import chisel3._
import chisel3.util._

class Combiner(exponentWidth: Int, additionSignificandWidth: Int, multiplicationSignificandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val addition = Input(new FloatingPoint(exponentWidth, additionSignificandWidth))
    val multiplication = Input(new FloatingPoint(exponentWidth, multiplicationSignificandWidth))
    val operation = Input(UInt(2.W))
    val output = Output(new FloatingPoint(exponentWidth, multiplicationSignificandWidth))
  })

  val addition = Wire(new FloatingPoint(exponentWidth, multiplicationSignificandWidth))
  addition := io.addition
  addition.significand := io.addition.significand ## io.addition.guard ## io.addition.round ## io.addition.sticky ## 0.U((multiplicationSignificandWidth - additionSignificandWidth - 3).W)
  addition.guard := 0.U
  addition.round := 0.U
  addition.sticky := 0.U

  val multiplication = io.multiplication

  io.output.sign := 0.U
  io.output.exponent := 0.U
  io.output.significand := 0.U
  io.output.guard := 0.U
  io.output.round := 0.U
  io.output.sticky := 0.U
  io.output.infinity := false.B
  io.output.denormal := false.B
  io.output.zero := false.B
  io.output.inexact := false.B
  io.output.nan := false.B

  switch (io.operation) {
    is (0.U) { io.output := addition }
    is (1.U) { io.output := addition }
    is (2.U) { io.output := multiplication }
  }
}
