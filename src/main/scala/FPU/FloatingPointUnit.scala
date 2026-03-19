package FloatingPointUnit

import chisel3._
import chisel3.util._

class FloatingPointUnit extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  val exponentWidth = 8
  val significandWidth = 24

  // Decode floating points
  val input1 = FloatingPoint.decode(io.a)
  val input2 = FloatingPoint.decode(io.b)

  // Adder
  val adder = Module(new Adder(exponentWidth, significandWidth))
  adder.io.input1 := input1
  adder.io.input2 := input2

  // Normalizer
  val normalizer = Module(new Normalizer(exponentWidth, significandWidth))
  normalizer.io.input := adder.io.output

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := normalizer.io.output

  // Renormalizer
  val renormalizer = Module(new Normalizer(exponentWidth, significandWidth))
  renormalizer.io.input := rounder.io.output

  // Encode output
  io.res := renormalizer.io.output.encode()
}

object FloatingPointUnit extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FloatingPointUnit)
}
