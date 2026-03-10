package FPU

import chisel3._

class FloatingPoint extends Bundle {
  val sign = UInt(1.W)
  val exponent = UInt(32.W) // TODO: make size parameterizable
  val significand = UInt(32.W) // TODO: make size parameterizable

  def encode(): UInt = sign ## exponent(7,0) ## significand(22,0) // TODO: subtract bias
}

object FloatingPoint {
  def decode(input: UInt): FloatingPoint = {
    val floatingPoint = Wire(new FloatingPoint)
    floatingPoint.sign := input(31)
    floatingPoint.exponent := input(30,23) // TODO: add bias
    floatingPoint.significand := 1.U(1.W) ## input(22,0)
    floatingPoint
  }
}
