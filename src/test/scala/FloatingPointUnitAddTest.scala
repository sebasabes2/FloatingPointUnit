import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FloatingPointUnitAddTest extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "add numbers of similar size and same sign" in {
    test(new FloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 1.0f)
      FloatingPointUnitAddTest(dut, 1.0f, 0.5f)
      FloatingPointUnitAddTest(dut, 0.5f, 1.0f)
      FloatingPointUnitAddTest(dut, -1.0f, -0.5f)
      FloatingPointUnitAddTest(dut, -0.5f, -1.0f)
    }
  }
  
  "FloatingPointUnit" should "add numbers of similar size and opposite sign" in {
    test(new FloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.75f, -0.5f)
      FloatingPointUnitAddTest(dut, -0.5f, 1.75f)
      FloatingPointUnitAddTest(dut, -1.75f, 0.5f)
      FloatingPointUnitAddTest(dut, 0.5f, -1.75f)
      FloatingPointUnitAddTest(dut, 1.0f, -2.0f)
      FloatingPointUnitAddTest(dut, -2.0f, 1.0f)
      FloatingPointUnitAddTest(dut, 1.0f, -1.25f)
      FloatingPointUnitAddTest(dut, -1.25f, 1.0f)
    }
  }

  "FloatingPointUnit" should "normalize result of much smaller size" in {
    test(new FloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, -1.0f + 1.0f / 128.0f)
    }
  }

  "FloatingPointUnit" should "round result to nearest tie to even" in {
    test(new FloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 0.2500001f)
    }
  }

  "FloatingPointUnit" should "round result up and renormalize down" in {
    test(new FloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 0.99999994f)
    }
  }
}

object FloatingPointUnitAddTest {
  def apply(dut: FloatingPointUnit, input1: Float, input2: Float) = FloatingPointUnitTest(dut, input1, input2, input1 + input2)
}
