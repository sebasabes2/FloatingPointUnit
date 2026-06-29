import floatingPointUnit.RightNormalizer
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RightNormalizerTest extends AnyFlatSpec with ChiselScalatestTester {
  "RightNormalizer" should "overflow" in {
    test(new RightNormalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x0".U)
      dut.io.input.exponent.poke("xFE".U)
      dut.io.input.significand.poke("x1000000".U)
      dut.io.roundingMode.poke(0.U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("xFF".U)
      dut.io.output.significand.expect("x800000".U)
      dut.io.output.infinity.expect(1.U)
    }
  }

  "RightNormalizer" should "refuse overflow" in {
    test(new RightNormalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x0".U)
      dut.io.input.exponent.poke("xFE".U)
      dut.io.input.significand.poke("x1000000".U)
      dut.io.roundingMode.poke(1.U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("xFE".U)
      dut.io.output.significand.expect("xFFFFFF".U)
      dut.io.output.infinity.expect(0.U)
    }
  }

  "RightNormalizer" should "pass ieee754-test-suite/Overflow.fptest:83" in {
    test(new RightNormalizer(8, 47)) { dut =>
      dut.io.input.sign.poke("x0".U)
      dut.io.input.exponent.poke("xFE".U)
      dut.io.input.significand.poke("x800000000000".U)
      dut.io.roundingMode.poke(1.U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("xFE".U)
      dut.io.output.significand.expect("x7FFFFFFFFFFF".U)
      dut.io.output.infinity.expect(0.U)
    }
  }
}
