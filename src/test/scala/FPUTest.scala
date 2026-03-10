import FPU._
import chisel3._
import chiseltest._

object FPUTest {
  def apply(dut: FPU, input1: Float, input2: Float, expected: Float) {
    dut.io.a.poke(("x" + java.lang.Float.floatToIntBits(input1).toHexString).U)
    dut.io.b.poke(("x" + java.lang.Float.floatToIntBits(input2).toHexString).U)
    dut.io.res.expect(("x" + java.lang.Float.floatToIntBits(expected).toHexString).U)
  }
}
