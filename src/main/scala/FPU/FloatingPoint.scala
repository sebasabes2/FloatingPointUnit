package FloatingPointUnit

import chisel3._

class FloatingPoint(exponentWidth: Int, significandWidth: Int) extends Bundle {
  val sign = UInt(1.W)
  val exponent = UInt(exponentWidth.W)
  val significand = UInt(significandWidth.W)

  val guard = UInt(1.W)
  val round = UInt(1.W)
  val sticky = UInt(1.W)

  def encode(): UInt = sign ## exponent ## significand(significandWidth - 2,0) // TODO: subtract bias
}

object FloatingPoint {
  def decode(input: UInt): FloatingPoint = {
    val floatingPoint = Wire(new FloatingPoint(8, 24))
    floatingPoint.sign := input(31)
    floatingPoint.exponent := input(30,23) // TODO: add bias
    floatingPoint.significand := 1.U(1.W) ## input(22,0)
    floatingPoint.guard := 0.U
    floatingPoint.round := 0.U
    floatingPoint.sticky := 0.U
    floatingPoint
  }
}
