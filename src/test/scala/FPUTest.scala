import FPU._
import chisel3._
import chiseltest._

object FPUTest {
  def apply(dut: FPU, input1: Float, input2: Float, expected: Float) {
    dut.io.a.poke(java.lang.Float.floatToIntBits(input1).U)
    dut.io.b.poke(java.lang.Float.floatToIntBits(input2).U)
    dut.io.res.expect(java.lang.Float.floatToIntBits(expected).U)
  }
}
