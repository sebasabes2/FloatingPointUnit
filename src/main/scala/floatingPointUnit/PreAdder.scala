package floatingPointUnit

import chisel3._

class PreAdder(exponentWidth: Int, significandWidth: Int) extends Module {
  val io = IO(new Bundle {
    val larger = Input(new FloatingPoint(exponentWidth, significandWidth))
    val smaller = Input(new FloatingPoint(exponentWidth, significandWidth))
    val input1 = Output(new FloatingPoint(exponentWidth, significandWidth + 3))
    val input2 = Output(new FloatingPoint(exponentWidth, significandWidth + 3))
    val subtract = Output(Bool())
  })

  val subtract = io.larger.sign.asBool ^ io.smaller.sign.asBool

  io.input1 := io.larger
  io.input1.significand := io.larger.significandWithRoundBits()
  io.input2 := io.smaller
  io.input2.significand := Mux(subtract, ~io.smaller.significandWithRoundBits(), io.smaller.significandWithRoundBits())
  io.subtract := subtract
}
