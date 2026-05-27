import floatingPointUnit.FloatingPointUnit
import floatingPointUnit.SinglePrecisionFloatingPointUnit
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FloatingPointUnitAddTest extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "add numbers of similar size and same sign" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 1.0f)
      FloatingPointUnitAddTest(dut, 1.0f, 0.5f)
      FloatingPointUnitAddTest(dut, 0.5f, 1.0f)
      FloatingPointUnitAddTest(dut, -1.0f, -0.5f)
      FloatingPointUnitAddTest(dut, -0.5f, -1.0f)
    }
  }

  "FloatingPointUnit" should "add numbers of similar size and opposite sign" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
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
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, -1.0f + 1.0f / 128.0f)
    }
  }

  "FloatingPointUnit" should "round result to nearest tie to even" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 0.2500001f)
    }
  }

  "FloatingPointUnit" should "round result up and renormalize down" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 1.0f, 0.99999994f)
    }
  }

  "FloatingPointUnit" should "add number with zero" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 5.877472e-38f, 0.0f)
    }
  }

  "FloatingPointUnit" should "add zero with zero" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 0.0f, 0.0f)
    }
  }

  "FloatingPointUnit" should "add negative zero with zero" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, -0.0f, 0.0f)
    }
  }

  "FloatingPointUnit" should "add zero with negative zero" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 0.0f, -0.0f)
    }
  }

  "FloatingPointUnit" should "add negative zero with negative zero" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, -0.0f, -0.0f)
    }
  }

  "FloatingPointUnit" should "add normal with denormal" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 2.3693558e-38f, 2.938736e-39f)
    }
  }

  "FloatingPointUnit" should "add numbers giving infinity" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 2.5521177e+38f, 2.5521177e+38f)
      FloatingPointUnitAddTest(dut, 3.4028234e+38f, 6.246974e+34f)
      FloatingPointUnitAddTest(dut, -2.5521177e+38f, -2.5521177e+38f)
      FloatingPointUnitAddTest(dut, -3.4028234e+38f, -6.246974e+34f)
    }
  }

  "FloatingPointUnit" should "add numbers with infinity" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 5.0f, Float.PositiveInfinity)
      FloatingPointUnitAddTest(dut, Float.PositiveInfinity, 3.14f)
      FloatingPointUnitAddTest(dut, 5.0f, Float.NegativeInfinity)
      FloatingPointUnitAddTest(dut, Float.NegativeInfinity, 3.14f)
      FloatingPointUnitAddTest(dut, Float.PositiveInfinity, Float.PositiveInfinity)
      FloatingPointUnitAddTest(dut, Float.NegativeInfinity, Float.NegativeInfinity)
    }
  }

  "FloatingPointUnit" should "add numbers giving NaN" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, Float.PositiveInfinity, Float.NegativeInfinity)
      FloatingPointUnitAddTest(dut, Float.NegativeInfinity, Float.PositiveInfinity)
    }
  }

  "FloatingPointUnit" should "add numbers with NaN" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      FloatingPointUnitAddTest(dut, 3.14f, Float.NaN)
      FloatingPointUnitAddTest(dut, Float.NaN, 12.123f)
      FloatingPointUnitAddTest(dut, Float.NaN, 0.0f)
      FloatingPointUnitAddTest(dut, 0.0f, Float.NaN)
      FloatingPointUnitAddTest(dut, Float.PositiveInfinity, Float.NaN)
      FloatingPointUnitAddTest(dut, Float.NaN, Float.PositiveInfinity)
      FloatingPointUnitAddTest(dut, Float.NaN, Float.NaN)
      FloatingPointUnitAddTest(dut, Float.NaN, -Float.NaN)
      FloatingPointUnitAddTest(dut, -Float.NaN, Float.NaN)
      FloatingPointUnitAddTest(dut, -Float.NaN, -Float.NaN)
    }
  }
}

object FloatingPointUnitAddTest {
  def apply(dut: FloatingPointUnit, input1: Float, input2: Float) = FloatingPointUnitTest(dut, input1, input2, input1 + input2)
}
