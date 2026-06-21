package fpuTester

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import floatingPointUnit.Stages
import floatingPointUnit.SinglePrecisionFloatingPointUnit
import floatingPointUnit.HalfPrecisionFloatingPointUnit

class SinglePrecisionFpuTester extends Module {
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

  val ctrlReg = Reg(UInt(5.W))
  val input1reg = Reg(Vec(4, UInt(8.W)))
  val input2reg = Reg(Vec(4, UInt(8.W)))

  when(btn) {
    switch(select) {
      is (0.U) { input1reg(index) := value }
      is (1.U) { input2reg(index) := value }
      is (2.U) { ctrlReg := value }
    }
  }

  // FloatingPointUnit

  val input1 = input1reg(3) ## input1reg(2) ## input1reg(1) ## input1reg(0)
  val input2 = input2reg(3) ## input2reg(2) ## input2reg(1) ## input2reg(0)

  val stages = new Stages {
    this.input = true
    this.output = true
    this.adder = true
    this.multiplier = true
    this.rightNormalizer = true
    this.shortener = true
  }

  val fpu = Module(new SinglePrecisionFloatingPointUnit(stages))

  fpu.io.operation := ctrlReg(1,0)
  fpu.io.roundingMode := ctrlReg(4,2)
  fpu.io.input1 := input1
  fpu.io.input2 := input2

  val output = Reg(UInt(32.W))
  output := fpu.io.output

  // Output

  val led = WireDefault(0.U(16.W))

  switch(select ## index(1)) {
    is (0.U) { led := input1reg(1) ## input1reg(0) }
    is (1.U) { led := input1reg(3) ## input1reg(2) }
    is (2.U) { led := input2reg(1) ## input2reg(0) }
    is (3.U) { led := input2reg(3) ## input2reg(2) }
    is (4.U) { led := ctrlReg }
    is (5.U) { led := ctrlReg }
    is (6.U) { led := output(15,0) }
    is (7.U) { led := output(31,16) }
  }

  io.led := RegNext(led)
}


class HalfPrecisionFpuTester extends Module {
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

  val ctrlReg = Reg(UInt(5.W))
  val input1reg = Reg(Vec(4, UInt(8.W)))
  val input2reg = Reg(Vec(4, UInt(8.W)))

  when(btn) {
    switch(select) {
      is (0.U) { input1reg(index) := value }
      is (1.U) { input2reg(index) := value }
      is (2.U) { ctrlReg := value }
    }
  }

  // FloatingPointUnit

  val input1 = input1reg(3) ## input1reg(2) ## input1reg(1) ## input1reg(0)
  val input2 = input2reg(3) ## input2reg(2) ## input2reg(1) ## input2reg(0)

  val stages = new Stages {
    this.input = true
    this.output = true
    this.selector = true
    this.shortener = true
  }

  val fpu = Module(new HalfPrecisionFloatingPointUnit(stages))

  fpu.io.operation := ctrlReg(1,0)
  fpu.io.roundingMode := ctrlReg(4,2)
  fpu.io.input1 := input1
  fpu.io.input2 := input2

  val output = Reg(UInt(32.W))
  output := fpu.io.output

  // Output

  val led = WireDefault(0.U(16.W))

  switch(select ## index(1)) {
    is (0.U) { led := input1reg(1) ## input1reg(0) }
    is (1.U) { led := input1reg(3) ## input1reg(2) }
    is (2.U) { led := input2reg(1) ## input2reg(0) }
    is (3.U) { led := input2reg(3) ## input2reg(2) }
    is (4.U) { led := ctrlReg }
    is (5.U) { led := ctrlReg }
    is (6.U) { led := output(15,0) }
    is (7.U) { led := output(31,16) }
  }

  io.led := RegNext(led)
}

object SinglePrecisionFpuTester extends App {
  emitVerilog(new SinglePrecisionFpuTester)
}

object HalfPrecisionFpuTester extends App {
  emitVerilog(new HalfPrecisionFpuTester)
}
