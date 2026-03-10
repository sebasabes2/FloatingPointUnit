package FPU

import chisel3._
import chisel3.util._

class FPU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  val a_significant = 1.U(1.W) ## io.a(22,0)
  val a_exponent = io.a(30,23)

  val b_significant = 1.U(1.W) ## io.b(22,0)
  val b_exponent = io.b(30,23)

  val added_significant = a_significant +& b_significant;

  // Convert to FloatingPoint Bundle
  // TODO: do this earlier

  val addedFloatingPoint = Wire(new FloatingPoint)
  addedFloatingPoint.exponent := a_exponent
  addedFloatingPoint.significant := added_significant

  // Normalizer

  val normalizer = Module(new Normalizer)
  normalizer.io.in := addedFloatingPoint

  // Encode output

  io.res := normalizer.io.out.encode()
}

object FPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FPU)
}
