package FPU

import chisel3._
import chisel3.util._

class FPU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  // // Decode floating points

  val input1 = FloatingPoint.decode(io.a)
  val input2 = FloatingPoint.decode(io.b)

  // Significand Adder

  val significandAdder = Module(new SignificandAdder)
  significandAdder.io.larger := input1
  significandAdder.io.smaller := input2

  // Normalizer

  val normalizer = Module(new Normalizer)
  normalizer.io.in := significandAdder.io.out

  // Encode output

  io.res := normalizer.io.out.encode()
}

object FPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FPU)
}
