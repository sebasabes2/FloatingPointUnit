package FPU

import chisel3._
import chisel3.util._

class FPU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  // Decode floating points
  val input1 = FloatingPoint.decode(io.a)
  val input2 = FloatingPoint.decode(io.b)

  // Exponent Matcher
  val exponentMatcher = Module(new ExponentMatcher)
  exponentMatcher.io.input1 := input1
  exponentMatcher.io.input2 := input2

  // Significand Adder
  val significandAdder = Module(new SignificandAdder)
  significandAdder.io.larger := exponentMatcher.io.larger
  significandAdder.io.smaller := exponentMatcher.io.smaller

  // Normalizer
  val normalizer = Module(new Normalizer)
  normalizer.io.in := significandAdder.io.out

  // Encode output
  io.res := normalizer.io.out.encode()
}

object FPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FPU)
}
