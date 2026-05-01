import floatingPointUnit.FloatingPointUnit
import chisel3._
import chiseltest._

object FloatingPointUnitTest {
  def apply(dut: FloatingPointUnit, input1: Float, input2: Float, expected: Float) {
    dut.io.input1.poke(("x" + java.lang.Float.floatToIntBits(input1).toHexString).U)
    dut.io.input2.poke(("x" + java.lang.Float.floatToIntBits(input2).toHexString).U)
    dut.clock.step(6)
    dut.io.output.expect(("x" + java.lang.Float.floatToIntBits(expected).toHexString).U)
  }
}
