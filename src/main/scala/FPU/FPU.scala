package FPU

import chisel3._
import chisel3.util._

class FPU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  val a_significand = 1.U(1.W) ## io.a(22,0)
  val a_exponent = io.a(30,23)

  val b_significand = 1.U(1.W) ## io.b(22,0)
  val b_exponent = io.b(30,23)

  // Decode floating points
  // Move this to FloatingPoint class

  val input1 = Wire(new FloatingPoint)
  input1.exponent := a_exponent
  input1.significand := a_significand

  val input2 = Wire(new FloatingPoint)
  input2.exponent := b_exponent
  input2.significand := b_significand

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
