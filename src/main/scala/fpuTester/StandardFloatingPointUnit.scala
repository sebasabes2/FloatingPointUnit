package fpuTester

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import floatingPointUnit.Stages
import floatingPointUnit.SinglePrecisionFloatingPointUnit

class StandardFloatingPointUnit extends SinglePrecisionFloatingPointUnit(new Stages { input = true; output = true })

object StandardFloatingPointUnit extends App {
  emitVerilog(new StandardFloatingPointUnit)
}
