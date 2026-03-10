import FPU._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AddTest extends AnyFlatSpec with ChiselScalatestTester {
  "FPU" should "pass" in {
    test(new FPU()) { dut =>
      FPUTest(dut, 1.0f, 1.0f, 2.0f)
    }
  }
}
