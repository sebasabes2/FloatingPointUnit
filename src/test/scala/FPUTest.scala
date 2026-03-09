import FPU._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FPUTest extends AnyFlatSpec with ChiselScalatestTester {
  "FPU" should "pass" in {
    test(new FPU()) { dut =>
      dut.io.a.poke(java.lang.Float.floatToIntBits(1.0f).U)
      dut.io.b.poke(java.lang.Float.floatToIntBits(1.0f).U)
      dut.io.res.expect(java.lang.Float.floatToIntBits(2.0f).U)
    }
  }
}
