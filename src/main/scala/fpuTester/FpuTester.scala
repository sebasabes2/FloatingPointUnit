package fpuTester

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import floatingPointUnit.FloatingPointUnit

class FpuTester extends Module {
  val io = IO(new Bundle {
    val sw = Input(UInt(16.W))
    val btn = Input(Bool())
    val led = Output(UInt(16.W))
  })

  // Input

  val sw = RegNext(RegNext(io.sw))
  val btn = RegNext(RegNext(io.btn))

  val select = sw(15,10)
  val index = sw(9,8)
  val value = sw(7,0)

  // State 

  val input1reg = Reg(Vec(4, UInt(8.W)))
  val input2reg = Reg(Vec(4, UInt(8.W)))

  when(btn) {
    switch(select) {
      is (0.U) { input1reg(index) := value }
      is (1.U) { input2reg(index) := value }
    }
  }

  // FloatingPointUnit

  val input1 = input1reg(3) ## input1reg(2) ## input1reg(1) ## input1reg(0)
  val input2 = input2reg(3) ## input2reg(2) ## input2reg(1) ## input2reg(0)

  val fpu = Module(new FloatingPointUnit)

  fpu.io.operation := 0.U
  fpu.io.roundingMode := 0.U
  fpu.io.input1 := RegNext(input1)
  fpu.io.input2 := RegNext(input2)

  val output = RegNext(fpu.io.output)

  // Output

  val led = WireDefault(0.U(16.W))

  switch(select ## index(1)) {
    is (0.U) { led := input1reg(1) ## input1reg(0) }
    is (1.U) { led := input1reg(3) ## input1reg(2) }
    is (2.U) { led := input2reg(1) ## input2reg(0) }
    is (3.U) { led := input2reg(3) ## input2reg(2) }
    is (4.U) { led := output(15,0) }
    is (5.U) { led := output(31,16) }
  }

  io.led := RegNext(led)
}

object FpuTester extends App {
  emitVerilog(new FpuTester)
}
