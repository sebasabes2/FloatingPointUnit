import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RenormalizeTest extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass" in {
    test(new FloatingPointUnit()) { dut =>
      AddTest(dut, 1.0f, 0.99999994f)
    }
  }
}
