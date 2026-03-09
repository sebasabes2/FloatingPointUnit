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

  val should_normalize = added_significant(24).asBool

  val res_significant = Mux(should_normalize, added_significant(24,1), added_significant(23,0))
  val res_exponent = Mux(should_normalize, a_exponent + 1.U, a_exponent)

  io.res := Cat(0.U(1.W), res_exponent, res_significant(22,0))
}

object FPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FPU)
}
