import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SubtractTest extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass" in {
    test(new FloatingPointUnit()) { dut =>
      AddTest(dut, 1.75f, -0.5f)
      AddTest(dut, -0.5f, 1.75f)
      AddTest(dut, -1.75f, 0.5f)
      AddTest(dut, 0.5f, -1.75f)
      AddTest(dut, 1.0f, -2.0f)
      AddTest(dut, -2.0f, 1.0f)
      AddTest(dut, 1.0f, -1.25f)
      AddTest(dut, -1.25f, 1.0f)
    }
  }
}
