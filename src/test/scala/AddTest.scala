import FPU._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AddTest extends AnyFlatSpec with ChiselScalatestTester {
  "FPU" should "pass" in {
    test(new FPU()) { dut =>
      AddTest(dut, 1.0f, 1.0f)
      AddTest(dut, 1.0f, 0.5f)
      AddTest(dut, 0.5f, 1.0f)
      AddTest(dut, -1.0f, -0.5f)
    }
  }
}

object AddTest {
  def apply(dut: FPU, input1: Float, input2: Float) = FPUTest(dut, input1, input2, input1 + input2)
}
