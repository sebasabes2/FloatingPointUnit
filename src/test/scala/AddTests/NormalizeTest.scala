import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class NormalizeTest extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass" in {
    test(new FloatingPointUnit()) { dut =>
      AddTest(dut, 1.0f, 1.0f - 1.0f / 128.0f)
    }
  }
}
